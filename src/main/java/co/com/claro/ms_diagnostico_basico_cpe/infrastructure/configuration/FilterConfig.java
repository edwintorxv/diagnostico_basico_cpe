package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 6/09/2024
 */
// Indica que esta clase es una configuración de Spring.
@Configuration
public class FilterConfig { // Define la configuración para registrar y aplicar filtros en la aplicación.

    // Declara un bean de tipo FilterRegistrationBean que estará disponible en el contexto de Spring.
    @Bean
    public FilterRegistrationBean<IpLoggingFilter> loggingFilter() {
        // Crear una instancia de FilterRegistrationBean para registrar un filtro personalizado.
        FilterRegistrationBean<IpLoggingFilter> registrationBean = new FilterRegistrationBean<>();

        // Configurar el filtro que se registrará.
        registrationBean.setFilter(new IpLoggingFilter()); // Asigna una instancia de IpLoggingFilter como el filtro a aplicar.

        // Especifica las URL a las que se aplicará este filtro.
        registrationBean.addUrlPatterns("/*"); // Aplica el filtro a todas las rutas de la aplicación.

        // Devuelve el registro del filtro para que Spring lo gestione.
        return registrationBean;
    }
}
