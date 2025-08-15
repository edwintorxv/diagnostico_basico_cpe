package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 18/09/2024
 */

// Anotación que indica que esta clase es una configuración de Spring.
@Configuration

// Habilita la configuración personalizada de seguridad web mediante Spring Security.
@EnableWebSecurity

// Anotación de Lombok que genera un constructor con todos los argumentos para esta clase.
@AllArgsConstructor
public class SecurityConfig { // Clase que configura la seguridad de la aplicación.

    /**
     * Configura la cadena de filtros de seguridad de la aplicación.
     *
     * @param http Objeto HttpSecurity para configurar la seguridad HTTP.
     * @return Cadena de filtros de seguridad configurada.
     * @throws Exception Si ocurre algún error durante la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configuración principal de la seguridad HTTP.
        http.csrf(AbstractHttpConfigurer::disable) // Deshabilita la protección CSRF (Cross-Site Request Forgery).
                .authorizeHttpRequests(auth -> auth
                        // Permite el acceso sin autenticación a todos los endpoints.
                        .requestMatchers("/**").permitAll()
                        // Cualquier otra solicitud que no coincida debe estar autenticada.
                        .anyRequest().authenticated())
                // Configura la gestión de sesiones como 'sin estado', adecuado para APIs RESTful.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Devuelve la cadena de filtros de seguridad configurada.
        return http.build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKeySpec secretKey = new SecretKeySpec("miClaveSuperSecreta1234567890".getBytes(), "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec("miClaveSuperSecreta1234567890".getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}