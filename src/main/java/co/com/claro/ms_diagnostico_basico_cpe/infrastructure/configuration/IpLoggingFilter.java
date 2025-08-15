package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration;


import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsConfig;
import jakarta.servlet.*;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 6/09/2024
 */
// Anotación de Lombok que genera automáticamente un logger para esta clase.
@Slf4j
public class IpLoggingFilter implements Filter { // Clase que implementa un filtro para registrar o procesar la IP del cliente.

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Método llamado cuando el filtro se inicializa.
        // Este método puede usarse para configurar parámetros o inicializar recursos necesarios para el filtro.
        // En este caso, no se requiere configuración adicional, por lo que se deja vacío.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // El método principal del filtro, donde se define la lógica que se ejecutará para cada solicitud.

        // Convertir la solicitud genérica en una HttpServletRequest para acceder a características específicas de HTTP.
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Intentar obtener la dirección IP del cliente desde la cabecera HTTP "X-Forwarded-For",
        // que es utilizada comúnmente por proxies y balanceadores de carga.
        String clientIp = httpRequest.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            // Si la cabecera "X-Forwarded-For" no está presente o está vacía,
            // obtener la IP directamente desde el cliente usando `getRemoteAddr`.
            clientIp = httpRequest.getRemoteAddr();
        }

        // Guardar la dirección IP obtenida en un atributo del request para que esté disponible en el resto del procesamiento.
        httpRequest.setAttribute(ConstantsConfig.CLIENT_IP, clientIp);

        // Continuar con el procesamiento de la solicitud, pasando al siguiente filtro o al controlador.
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Método llamado cuando el filtro es destruido.
        // Se utiliza para liberar recursos asignados en el filtro, si es necesario.
        // En este caso, no se necesita lógica de limpieza, por lo que se deja vacío.
    }
}
