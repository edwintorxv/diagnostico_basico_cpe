package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Configuración de Swagger/OpenAPI para la documentación de la API.
 *
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 6/09/2024
 */
// Importación del esquema de seguridad para Swagger/OpenAPI.
@SecurityScheme(
        name = "bearerAuth", // Define el nombre del esquema de seguridad, en este caso, "bearerAuth".
        type = SecuritySchemeType.HTTP, // Especifica el tipo de esquema de seguridad, aquí es HTTP.
        scheme = "bearer", // Indica que el esquema utilizado es "Bearer" para tokens.
        bearerFormat = "JWT" // Especifica que el formato del token es JWT (JSON Web Token).
)
@Configuration
public class SwaggerConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.build.date:unknown}")
    private String buildDate;

    /**
     * Configura la documentación de OpenAPI, asegurando que la fecha de compilación
     * se muestre en la zona horaria de Bogotá (UTC-5).
     *
     * @return la configuración de OpenAPI con la información de la API.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationName.toUpperCase())
                        .version(convertBuildDateToBogotaTime(buildDate))
                        .description(String.format("Esta API proporciona los servicios de %s. Consulte la documentación para más detalles.", applicationName))
                );
    }

    /**
     * Convierte la fecha de compilación en formato UTC a la zona horaria de Bogotá (UTC-5).
     *
     * @param utcDate Fecha en formato UTC.
     * @return Fecha formateada en zona horaria de Bogotá.
     */
    private String convertBuildDateToBogotaTime(String utcDate) {
        try {
            Instant instant = Instant.parse(utcDate);
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.of("America/Bogota"))
                    .format(instant);
        } catch (Exception e) {
            return utcDate; // Si hay un error, devuelve la fecha original
        }
    }
}
