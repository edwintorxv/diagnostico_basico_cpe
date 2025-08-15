package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 18/12/2024
 */
public class ConstantsLog {
    public static final String LOG4J_REQUEST_IP = "-- SOLICITUD RECIBIDA | IP: %s | MÉTODO: %s | ENDPOINT: %s";
    public static final String LOG4J_METHOD_START = "-- INICIO DE TRANSACCIÓN | MÉTODO: [ %s ]";
    public static final String LOG4J_METHOD_END = "-- FIN DE TRANSACCIÓN | MÉTODO: [ %s ]";
    public static final String LOG4J_REQUEST = "-- SOLICITUD RECIBIDA | REQUEST: %s";
    public static final String LOG4J_RESPONSE = "-- RESPUESTA GENERADA | RESPONSE: %s";
    public static final String LOG4J_STATUS = "-- ESTADO DE RESPUESTA | CÓDIGO: %s";
    public static final String LOG4J_TRANSACTION_TIME = "-- TIEMPO DE EJECUCIÓN | DURACIÓN: %s ms";
    public static final String LOG4J_INPUT_PARAMETERS = "-- PARÁMETROS DE ENTRADA: %s";
    public static final String LOG4J_OUTPUT_PARAMETERS = "-- PARÁMETROS DE SALIDA: %s";
    public static final String LOG4J_VARIABLE = "-- VARIABLE REGISTRADA | NOMBRE Y VALOR: %s";
    public static final String LOG4J_LDAP = "-- RESPUESTA LDAP | USUARIO: [ %s ] | DETALLE: %s";
    public static final String LOG4J_PARAMETERS = "-- CONFIGURACIÓN CARGADA | TOTAL PROPIEDADES: %s";
    public static final String LOG4J_WSDL = "-- WSDL CONSULTADO | URL: %s";
    public static final String LOG4J_METODO = "-- MÉTODO EJECUTADO | NOMBRE: %s";
    public static final String LOG4J_NAME_WS = "-- SERVICIO WEB | NOMBRE: %s";
    public static final String LOG4J_TIMEOUT = "-- TIEMPO DE ESPERA | DURACIÓN: %s segundos";
    public static final String LOG4J_RESPONSE_EXCEPTION = "-- EXCEPCIÓN EN RESPUESTA | DETALLE: %s";
    public static final String LOG4J_LOG_GENERATION_SUCCESS = "Operación completada con éxito. Log generado correctamente.";
    public static final String LOG4J_SWAGGER_GENERATION_SUCCESS = "Operación completada con éxito. URL de Swagger generada correctamente.";

}
