package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsConfig;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsLog;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.exceptions.DataValidationException;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.utils.configuration.UtilJson;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 28/11/2024
 */

// Anotación de Lombok que genera automáticamente un logger para esta clase.
@Slf4j
public class ParametersConfig { // Clase encargada de gestionar un archivo de propiedades y proporcionar acceso a sus valores.

    // Instancia de Properties para cargar y gestionar las propiedades del archivo.
    private static final Properties properties = new Properties();

    // Ruta completa del archivo de propiedades en el sistema de archivos.
    private static final String filePath = "/var/properties/%s.properties";
    /**
     * Método estático que inicializa la carga del archivo properties.
     * Se ejecuta automáticamente después de que el bean sea construido.
     */
    @PostConstruct
    public static void init(String appName) {
        try (FileInputStream input = new FileInputStream(String.format(filePath, appName));
             InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            properties.load(reader);
            log.info(String.format(ConstantsLog.LOG4J_PARAMETERS, UtilJson.toJson(properties.size())));
        } catch (Exception ex) {
            log.error(String.format(ConstantsLog.LOG4J_RESPONSE, UtilJson.toJson("Error al leer el archivo de propiedades: " + ex.getMessage())));
        }
    }

    /**
     * Método para obtener el valor de una propiedad especificada por su clave.
     *
     * @param key         Clave de la propiedad a buscar.
     * @param transaction Transacción actual que gestiona el tiempo de procesamiento.
     * @return Valor de la propiedad correspondiente a la clave.
     * @throws Exception Si no se encuentra la clave o ocurre algún error.
     */
    public static String getPropertyValue(String key, Transaction transaction) throws Exception {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new DataValidationException(transaction, String.format(ConstantsConfig.ERROR_GET_PARAMETER, key), ParametersConfig.class);
        }
        return value; // Retorna el valor encontrado.
    }
}