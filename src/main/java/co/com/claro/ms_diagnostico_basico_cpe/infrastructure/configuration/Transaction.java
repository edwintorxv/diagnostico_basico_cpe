package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration;


import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsLog;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 21/11/2024
 */
@Slf4j
@Getter
public class Transaction {
    private static final ThreadLocal<Transaction> currentTransaction = new ThreadLocal<>();
    private final Instant time;
    private final UUID uuid;

    /**
     * Metodo encargado de registrar en el log el tiempo de procesamiento de la
     * solicitud. Adicionalmente, genera la variable de UUID mostrada en el log.
     */
    protected Transaction() {
        this.time = Instant.now();
        this.uuid = UUID.randomUUID();
    }

    /**
     * Metodo encargado de retornar el tiempo inicial de la transaccion junto
     * con el id de transaccion. Configura el log para registrar el id de
     * transaccion
     *
     * @return Objeto con los parametros de tiempo inicial de la transaccion y
     * id de transaccion
     */
    public static Transaction startTransaction() {
        Transaction param = new Transaction();
        ThreadContext.put("UUID", param.getUuid().toString());
        ThreadContext.put("dynamicClass", Transaction.class.getSimpleName());
        ThreadContext.put("dynamicMethod", Thread.currentThread().getStackTrace()[1].getMethodName());
        currentTransaction.set(param);
        return param;
    }

    public static Transaction getCurrentTransaction() {
        return currentTransaction.get();
    }

    /**
     * Metodo encargado de generar en el Log el tiempo de procesamiento de la
     * transaccion. Adicionalmente limpia la variable de UUID mostrada en el
     * log.
     *
     * @param transaction
     * @param logger
     */
    public static void stopTransaction(Transaction transaction, Logger logger) {
        ThreadContext.put("dynamicClass", Transaction.class.getSimpleName());
        ThreadContext.put("dynamicMethod", Thread.currentThread().getStackTrace()[1].getMethodName());
        Instant endTime = Instant.now();
        Duration duration = Duration.between(transaction.getTime(), endTime);
        logger.info(String.format(ConstantsLog.LOG4J_TRANSACTION_TIME, duration.toMillis()));
        ThreadContext.clearAll();
    }
}

