package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.exceptions;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.context.ApplicationContextException;
import org.springframework.dao.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.SQLRecoverableException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Clase que maneja excepciones globales de la aplicación.
 * Permite capturar y gestionar de manera centralizada las excepciones
 * lanzadas por los controladores.
 *
 * @autor <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 3/07/2024
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String TRANSACTION = "transaction";
    private static final String DATE = "date";
    private static final String TYPE_ERROR = "typeError";
    private static final String FILE_ERROR = "fileError";
    private static final String METHOD = "method";
    private static final String ERROR_LINE = "errorLine";
    private static final String DETAIL_ERROR = "detailError";
    private static final String CAUSE_ERROR = "causeError";
    private static final String VALUE_NA = "N/A";
    private static final String UNKNOWN_CAUSE = "Causa desconocida.";

    /**
     * Maneja cualquier excepción lanzada por la aplicación, devolviendo una respuesta detallada.
     *
     * @param ex la excepción lanzada
     * @return ResponseEntity con el mapa de detalles del error y el estado HTTP correspondiente
     */
    @ExceptionHandler({
            SQLRecoverableException.class,
            InvalidDataAccessApiUsageException.class,
            DataAccessException.class,
            DataBaseException.class,
            JDBCConnectionException.class,
            DataValidationException.class,
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            WebServicesException.class,
            GeneralException.class,
            ResourceAccessException.class,
            Exception.class,
            ConnectException.class,
            ApplicationContextException.class,
            ResourceAccessException.class,
            SocketTimeoutException.class,
            ConnectException.class,
            UnknownHostException.class,
    })
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleAnyException(Exception ex) {
        try {
            ThreadContext.put("dynamicClass", this.getClass().getSimpleName());
            ThreadContext.put("dynamicMethod", Thread.currentThread().getStackTrace()[1].getMethodName());

            HttpStatus status = getHttpStatus(ex);

            // Construye la respuesta de error con detalles específicos
            Map<String, Object> errorResponse = buildErrorResponse(ex);

            // Registra los detalles del error en el log
            log.error("{}: {} {}: {} {}: {} {}: {} {}: {} {}:{} {}: {}",
                    TRANSACTION, errorResponse.get(TRANSACTION),
                    TYPE_ERROR, ((Throwable) ex).getClass().getSimpleName(),
                    FILE_ERROR, errorResponse.get(FILE_ERROR),
                    METHOD, errorResponse.get(METHOD),
                    ERROR_LINE, errorResponse.get(ERROR_LINE),
                    DETAIL_ERROR, errorResponse.get(DETAIL_ERROR),
                    CAUSE_ERROR, errorResponse.get(CAUSE_ERROR));
            return new ResponseEntity<>(errorResponse, status);
        } finally {
            ThreadContext.remove("dynamicClass");
            ThreadContext.remove("dynamicMethod");
        }
    }

    /**
     * Determina el estado HTTP a devolver basado en el tipo de excepción.
     *
     * @param ex la excepción capturada
     * @return el estado HTTP correspondiente
     */
    private HttpStatus getHttpStatus(Exception ex) {
        if (ex instanceof DataValidationException
                || ex instanceof InvalidDataAccessApiUsageException
                || ex instanceof MethodArgumentNotValidException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof WebServicesException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof JwtException || ex instanceof MissingRequestHeaderException) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * Construye un mapa con los detalles del error basados en la excepción proporcionada.
     *
     * @param cause la causa de la excepción
     * @return un mapa con los detalles del error
     */
    private Map<String, Object> buildErrorResponse(Throwable cause) {
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        Transaction transaction = Transaction.getCurrentTransaction() == null
                ? Transaction.startTransaction()
                : Transaction.getCurrentTransaction();
        ThreadContext.put("UUID", transaction.getUuid().toString());
        errorResponse.put(TRANSACTION, transaction.getUuid());
        errorResponse.put(DATE, LocalDateTime.now().format(formatter));
        errorResponse.put(TYPE_ERROR, cause.getClass().getSimpleName());

        // Obtiene el elemento relevante del stack trace para detalles del archivo y método
        StackTraceElement st = getRelevantStackTraceElement(cause.getStackTrace());
        errorResponse.put(FILE_ERROR, st != null ? st.getFileName() : VALUE_NA);
        errorResponse.put(METHOD, st != null ? st.getMethodName() : VALUE_NA);
        errorResponse.put(ERROR_LINE, st != null ? st.getLineNumber() : VALUE_NA);

        // Manejo de excepciones específicas y agregación de detalles
        if (cause instanceof DataBaseException dataBaseException) {
            populateErrorDetails(
                    errorResponse,
                    dataBaseException.getTransaction(),
                    ETypeError.BASEDATOS,
                    dataBaseException.getFunctionalMessage(),
                    cause
            );
        } else if (cause instanceof DataValidationException dataValidationException) {
            populateErrorDetails(
                    errorResponse,
                    dataValidationException.getTransaction(),
                    ETypeError.VALIDACION,
                    dataValidationException.getFunctionalMessage(),
                    cause
            );
        } else if (cause instanceof WebServicesException webServicesException) {
            populateErrorDetails(
                    errorResponse,
                    webServicesException.getTransaction(),
                    ETypeError.WEBSERVICES,
                    webServicesException.getFunctionalMessage(),
                    cause
            );
        } else if (cause instanceof MethodArgumentNotValidException methodArgumentNotValidException) {
            Map<String, String> errors = new HashMap<>();
            methodArgumentNotValidException.getBindingResult()
                    .getAllErrors()
                    .forEach(error -> errors.put("", error.getDefaultMessage()));
            String errorMessages = errors.values().stream()
                    .collect(Collectors.joining("\n"));
            populateErrorDetails(
                    errorResponse,
                    transaction,
                    ETypeError.BADREQUEST,
                    errorMessages,
                    cause
            );
        } else if (cause instanceof HttpMessageNotReadableException httpMessageException) {
            // Cambiado para dar prioridad a sintaxis JSON antes de propiedad desconocida:
            Throwable jacksonCause = Optional.ofNullable(httpMessageException.getCause())
                    .orElse(httpMessageException);

            if (jacksonCause instanceof JsonParseException) {
                // JSON mal formado (sintaxis inválida)
                String mensajeFuncional = "El JSON de la solicitud está mal formado. Revisa la sintaxis.";
                populateErrorDetails(
                        errorResponse,
                        transaction,
                        ETypeError.BADREQUEST,
                        mensajeFuncional,
                        httpMessageException
                );
            } else if (jacksonCause instanceof UnrecognizedPropertyException unrecognizedEx) {
                // Propiedad JSON desconocida en el DTO
                String propiedad = unrecognizedEx.getPropertyName();
                String mensajeFuncional = "La propiedad '" + propiedad
                        + "' no es reconocida en la solicitud JSON.";
                populateErrorDetails(
                        errorResponse,
                        transaction,
                        ETypeError.BADREQUEST,
                        mensajeFuncional,
                        httpMessageException
                );
            } else if (jacksonCause instanceof JsonMappingException) {
                // Otro error de mapeo (p. ej., tipo de dato incorrecto)
                String mensajeFuncional = "Error al mapear el JSON. Verifica que los campos coincidan.";
                populateErrorDetails(
                        errorResponse,
                        transaction,
                        ETypeError.BADREQUEST,
                        mensajeFuncional,
                        httpMessageException
                );
            } else {
                // Cualquier otro caso de HttpMessageNotReadableException
                String mensajeRaiz = getRootCauseMessage(jacksonCause);
                populateErrorDetails(
                        errorResponse,
                        transaction,
                        ETypeError.BADREQUEST,
                        mensajeRaiz,
                        httpMessageException
                );
            }
        } else if (cause instanceof JwtException
                || cause instanceof MissingRequestHeaderException) {
            errorResponse.put(DETAIL_ERROR, ETypeError.TOKEN.getDescription());
        } else if (cause instanceof DataAccessResourceFailureException
                || cause instanceof InvalidDataAccessResourceUsageException
                || cause instanceof DataIntegrityViolationException
                || cause instanceof TransactionSystemException) {
            errorResponse.put(DETAIL_ERROR, ETypeError.BASEDATOS.getDescription());
        } else if (cause instanceof ResourceAccessException
                || cause instanceof HttpClientErrorException
                || cause instanceof ConnectException) {
            errorResponse.put(DETAIL_ERROR, ETypeError.WEBSERVICES.getDescription());
        } else {
            errorResponse.put(DETAIL_ERROR, ETypeError.INTERNOS.getDescription());
        }

        if (!errorResponse.containsKey(CAUSE_ERROR)) {
            errorResponse.put(
                    CAUSE_ERROR,
                    cause.getMessage() != null ? cause.getMessage() : UNKNOWN_CAUSE
            );
        }

        return errorResponse;
    }

    /**
     * Llena los detalles del error en la respuesta basada en la transacción,
     * tipo de error y mensaje funcional.
     *
     * @param errorResponse     mapa de respuesta de error
     * @param transactionUuid   transacción relacionada con el error
     * @param typeError         tipo de error
     * @param functionalMessage mensaje funcional del error
     * @param cause             excepción técnica original
     */
    private void populateErrorDetails(
            Map<String, Object> errorResponse,
            Transaction transactionUuid,
            ETypeError typeError,
            String functionalMessage,
            Throwable cause
    ) {
        errorResponse.put(TRANSACTION, transactionUuid.getUuid());
        errorResponse.put(DETAIL_ERROR, typeError.getDescription());
        errorResponse.put(
                CAUSE_ERROR,
                (functionalMessage != null && !functionalMessage.trim().isEmpty())
                        ? functionalMessage
                        : getRootCauseMessage(cause)
        );
    }

    /**
     * Obtiene el elemento relevante del stack trace que no sea nativo.
     *
     * @param stackTraceElements array de elementos del stack trace
     * @return el primer elemento relevante o null si no se encuentra
     */
    private static StackTraceElement getRelevantStackTraceElement(StackTraceElement[] stackTraceElements) {
        return Optional.ofNullable(stackTraceElements)
                .flatMap(stack ->
                        Arrays.stream(stack)
                                .filter(element -> !element.isNativeMethod())
                                .findFirst()
                )
                .orElse(null);
    }

    /**
     * Obtiene la causa más profunda de una excepción.
     *
     * @param throwable Excepción capturada.
     * @return Throwable que representa la causa más profunda.
     */
    private Throwable getRootCause(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root != root.getCause()) {
            root = root.getCause();
        }
        return root;
    }

    /**
     * Obtiene el mensaje de la causa más profunda o un valor genérico si no está disponible.
     *
     * @param throwable Excepción capturada.
     * @return Mensaje de la causa raíz o "Causa desconocida".
     */
    private String getRootCauseMessage(Throwable throwable) {
        Throwable rootCause = getRootCause(throwable);
        return (rootCause.getMessage() != null && !rootCause.getMessage().isEmpty())
                ? rootCause.getMessage()
                : UNKNOWN_CAUSE;
    }

}
