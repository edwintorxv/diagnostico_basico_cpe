package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Configuración personalizada para el ObjectMapper de Jackson en Spring Boot.
 * Permite manejar correctamente fechas y establecer la zona horaria de Bogotá.
 *
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 27/09/2024
 */
@Configuration
public class JacksonConfig {

    /**
     * Configura y expone un {@link ObjectMapper} como bean en el contexto de Spring.
     * Se establece la zona horaria de "America/Bogota" y se registra el módulo JavaTimeModule
     * para serializar y deserializar correctamente objetos de fecha y hora.
     *
     * @return una instancia de {@link ObjectMapper} configurada.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Manejo de fechas y horas con el módulo JavaTimeModule
        objectMapper.registerModule(new JavaTimeModule());

        // Configurar la zona horaria por defecto a "America/Bogota"
        objectMapper.setTimeZone(TimeZone.getTimeZone(ZoneId.of("America/Bogota")));

        // Configura el ObjectMapper para que lance una excepción si encuentra propiedades desconocidas en JSON.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        return objectMapper;
    }
}
