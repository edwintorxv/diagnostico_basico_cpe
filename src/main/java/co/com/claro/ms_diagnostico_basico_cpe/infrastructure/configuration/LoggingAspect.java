package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsConfig;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsLog;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.utils.configuration.UtilJson;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.ThreadContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aspecto para la gestión de logs y transacciones en la aplicación.
 * Se encarga de capturar información sobre la ejecución de métodos
 * en diferentes capas, registrando tiempos de ejecución y datos de entrada/salida.
 *
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 13/03/2025
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Punto de corte para la capa de infraestructura en los controladores REST.
     */
    @Pointcut("execution(* *..infrastructure.rest..*(..))")
    public void logMethods() {
    }

    /**
     * Punto de corte para la capa de servicios, excluyendo configuración.
     */
    @Pointcut("execution(* *..application.service..*(..)) && " +
            "!execution(* *..application.service.usecase.configuration..*(..))")
    public void serviceLayer() {
    }

    /**
     * Punto de corte para la capa de repositorios.
     */
    @Pointcut("execution(* *..infrastructure.repository..*(..))")
    public void repositoryLayer() {
    }

    /**
     * Punto de corte para la configuración de parámetros.
     */
    @Pointcut("execution(* *..infrastructure.configuration.ParametersConfig..*(..))")
    public void configurationLayer() {
    }

    /**
     * Punto de corte para clientes REST.
     */
    @Pointcut("execution(* *..infrastructure.utils.configuration.UtilRestClient..*(..))")
    public void restClientLayer() {
    }

    /**
     * Punto de corte para clientes SOAP.
     */
    @Pointcut("execution(* *..infrastructure.utils.configuration.UtilSoapClient..*(..))")
    public void soapClientLayer() {
    }

    /**
     * Interceptor que gestiona la transacción y registra logs para los métodos en la capa REST.
     *
     * @param joinPoint Punto de ejecución del método interceptado
     * @return Resultado del método ejecutado
     * @throws Throwable En caso de error en la ejecución del método
     */
    @Around("logMethods()")
    public Object manageTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        Transaction transaction = Transaction.startTransaction();
        Object[] modifiedArgs = injectTransactionIfNeeded(joinPoint, transaction);

        HttpServletRequest request = null;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
            request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        }

        final String clientIp = (request != null) ? (String) request.getAttribute(ConstantsConfig.CLIENT_IP) : "N/A";
        final String httpMethod = (request != null) ? request.getMethod() : "N/A";
        final String requestUri = (request != null) ? request.getRequestURI() : "N/A";

        final String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        final String methodName = joinPoint.getSignature().getName();

        // Agregar información al contexto de Log4j2 para seguimiento
        ThreadContext.put("dynamicClass", className);
        ThreadContext.put("dynamicMethod", methodName);

        final List<Object> filteredArgs = Arrays.stream(joinPoint.getArgs())
                .filter(arg -> !(arg instanceof SecurityContextHolderAwareRequestWrapper))
                .filter(arg -> !(arg instanceof Transaction))
                .collect(Collectors.toList());

        log.info(String.format(ConstantsLog.LOG4J_REQUEST_IP, clientIp, httpMethod, requestUri));
        log.info(String.format(ConstantsLog.LOG4J_METHOD_START, className, methodName));
        log.info(String.format(ConstantsLog.LOG4J_REQUEST, UtilJson.toJson(filteredArgs)));

        Object response;
        try {
            response = joinPoint.proceed(modifiedArgs);

            // Agregar información al contexto de Log4j2 nuevamente
            ThreadContext.put("dynamicClass", className);
            ThreadContext.put("dynamicMethod", methodName);

            String logMessage = switch (className) {
                case "SwaggerController" -> ConstantsLog.LOG4J_SWAGGER_GENERATION_SUCCESS;
                case "LogController" -> ConstantsLog.LOG4J_LOG_GENERATION_SUCCESS;
                default -> UtilJson.toJson(response);
            };

            log.info(String.format(ConstantsLog.LOG4J_RESPONSE, UtilJson.toJson(logMessage)));
            log.info(String.format(ConstantsLog.LOG4J_METHOD_END, className, methodName));

            return response;
        } finally {
            Transaction.stopTransaction(transaction, log);
            ThreadContext.remove("dynamicClass");
            ThreadContext.remove("dynamicMethod");
        }
    }

    /**
     * Interceptor para registrar logs en la capa de servicio, repositorio, configuración y clientes REST/SOAP.
     */
    @Around("serviceLayer() || repositoryLayer() || configurationLayer() || restClientLayer() || soapClientLayer()")
    public Object logMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        Transaction transaction = getTransactionFromArgs(joinPoint);

        // Agregar información al contexto de Log4j2
        ThreadContext.put("dynamicClass", className);
        ThreadContext.put("dynamicMethod", methodName);

        List<Object> filteredArgs = Arrays.stream(joinPoint.getArgs())
                .filter(arg -> !(arg instanceof SecurityContextHolderAwareRequestWrapper))
                .filter(arg -> !(arg instanceof Transaction))
                .collect(Collectors.toList());

        try {
            log.info(String.format(ConstantsLog.LOG4J_METHOD_START, methodName));
            log.info(String.format(ConstantsLog.LOG4J_INPUT_PARAMETERS, UtilJson.toJson(filteredArgs)));

            Object response = joinPoint.proceed(joinPoint.getArgs());

            // Agregar información al contexto de Log4j2 nuevamente
            ThreadContext.put("dynamicClass", className);
            ThreadContext.put("dynamicMethod", methodName);

            log.info(String.format(ConstantsLog.LOG4J_OUTPUT_PARAMETERS, UtilJson.toJson(response)));
            log.info(String.format(ConstantsLog.LOG4J_METHOD_END, methodName));

            return response;
        } finally {
            if (transaction != null) {
                Instant endTime = Instant.now();
                Duration duration = Duration.between(transaction.getTime(), endTime);
                log.info(String.format(ConstantsLog.LOG4J_TRANSACTION_TIME, duration.toMillis()));
            }

            ThreadContext.remove("dynamicClass");
            ThreadContext.remove("dynamicMethod");
        }
    }

    /**
     * Extrae la transacción de los parámetros del método si está presente.
     */
    private Transaction getTransactionFromArgs(ProceedingJoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .filter(arg -> arg instanceof Transaction)
                .map(arg -> (Transaction) arg)
                .findFirst()
                .orElse(null);
    }

    /**
     * Si el método tiene un parámetro de tipo `Transaction`, lo reemplaza con la transacción activa.
     */
    private Object[] injectTransactionIfNeeded(ProceedingJoinPoint joinPoint, Transaction transaction) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Class<?>[] parameterTypes = methodSignature.getParameterTypes();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].equals(Transaction.class)) {
                args[i] = transaction;
            }
        }
        return args;
    }

}
