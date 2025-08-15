package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Spring MVC, incluyendo CORS y recursos estáticos.
 *
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 30/09/2024
 */
@Configuration
public class WebConfig implements WebMvcConfigurer { // Implementa WebMvcConfigurer para personalizar la configuración de Spring MVC.

    /**
     * Declara un bean que configura CORS (Cross-Origin Resource Sharing).
     *
     * @return Configuración personalizada para CORS.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() { // Retorna una implementación anónima de WebMvcConfigurer.
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Configura las reglas de CORS.
                registry.addMapping("/**") // Permite CORS en todas las rutas de la aplicación.
                        .allowedOrigins("*") // Permite solicitudes desde cualquier origen.
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite estos métodos HTTP.
                        .allowedHeaders("*") // Permite todas las cabeceras.
                        .allowCredentials(false); // No permite compartir cookies o credenciales.
            }
        };
    }
}