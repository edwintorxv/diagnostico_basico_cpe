package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.exceptions;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import lombok.Getter;
import lombok.Setter;

/**
 * GeneralException es una excepción personalizada que contiene información adicional
 * sobre errores específicos que pueden ocurrir en el sistema.
 *
 * @autor <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 3/07/2024
 */
@Getter
@Setter
public class GeneralException extends RuntimeException {

    private final Transaction transaction;
    private final String functionalMessage;
    private final String className;
    private final ETypeError errorType;

    protected GeneralException(Transaction transaction, String message, Throwable cause, Class<?> clazz, ETypeError errorType) {
        super(message, cause);
        this.transaction = transaction;
        this.functionalMessage = message;
        this.className = clazz.getName();
        this.errorType = errorType;
    }

    protected GeneralException(Transaction transaction, String functionalMessage, Class<?> clazz, ETypeError errorType) {
        super(functionalMessage);
        this.transaction = transaction;
        this.functionalMessage = functionalMessage;
        this.className = clazz.getName();
        this.errorType = errorType;
    }

    public GeneralException(Transaction transaction, String functionalMessage, Throwable cause, Class<?> clazz) {
        super(functionalMessage, cause);
        this.transaction = transaction;
        this.functionalMessage = functionalMessage;
        this.className = clazz.getName();
        this.errorType = null;
    }
}
