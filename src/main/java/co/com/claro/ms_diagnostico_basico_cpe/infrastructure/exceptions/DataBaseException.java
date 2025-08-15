package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.exceptions;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;

/**
 * Excepción para errores relacionados con bases de datos.
 *
 * @autor <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 3/07/2024
 */
public class DataBaseException extends GeneralException {
    public DataBaseException(Transaction transaction, String message, Throwable cause, Object classE) {
        super(transaction, message, cause, classE.getClass(), ETypeError.BASEDATOS);
    }

    public DataBaseException(Transaction transaction, String functionalMessage, Object classE) {
        super(transaction, functionalMessage, classE.getClass(), ETypeError.BASEDATOS);
    }
}
