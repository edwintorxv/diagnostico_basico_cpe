package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.exceptions;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;

/**
 * Excepción para errores de validación de datos.
 *
 * @autor <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 3/07/2024
 */
public class DataValidationException extends GeneralException {
    public DataValidationException(Transaction transaction, String message, Throwable cause, Object classE) {
        super(transaction, message, cause, classE.getClass(), ETypeError.VALIDACION);
    }

    public DataValidationException(Transaction transaction, String functionalMessage, Object classE) {
        super(transaction, functionalMessage, classE.getClass(), ETypeError.VALIDACION);
    }
}
