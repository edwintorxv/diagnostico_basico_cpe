package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.exceptions;

import lombok.Getter;

/**
 * Enumeración de tipos de error para clasificación y trazabilidad de excepciones.
 *
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 10/07/2024
 */
@Getter
public enum ETypeError {
    VALIDACION("Validación de Datos", "Se ha producido un error en la validación de datos."),
    INTERNOS("Error Interno", "Se ha producido un error inesperado en el sistema."),
    BASEDATOS("Base de Datos", "Se ha producido un error con la base de datos."),
    WEBSERVICES("Web Services", "Se ha producido un error en la comunicación con los servicios web."),
    TOKEN("Token", "Se ha producido un error en la autenticación del token."),
    BADREQUEST("Bad Request", "La solicitud es inválida o contiene errores.");


    private final String type;
    private final String description;

    ETypeError(String type, String description) {
        this.type = type;
        this.description = description;
    }
}
