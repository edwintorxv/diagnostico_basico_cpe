package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.utils.configuration;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.exceptions.WebServicesException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.*;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;

/**
 * Cliente REST utilitario para llamadas HTTP con manejo de errores.
 * <p>
 * Ejemplo de uso:
 * <p>
 * RestClientUtil client = new RestClientUtil();
 * ResponseEntity<String> response = client.get("https://api.example.com/resource", null, null);
 * System.out.println(response.getBody());
 *
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 26/11/2024
 */
@Slf4j
@Component
public class UtilRestClient {

    private final RestTemplate restTemplate;

    public UtilRestClient() {
        this.restTemplate = new RestTemplate();
    }

    public ResponseEntity<String> get(String url, Map<String, String> headers, Transaction transaction) throws WebServicesException {
        return executeRequest(url, headers, null, HttpMethod.GET, transaction);
    }

    public ResponseEntity<String> get(String url, Map<String, String> headers, Map<String, String> uriVariables, Transaction transaction) throws WebServicesException {
        URI uri = UriComponentsBuilder.fromUriString(url)
                .buildAndExpand(uriVariables != null ? uriVariables : Map.of())
                .toUri();
        return executeRequest(uri.toString(), headers, null, HttpMethod.GET, transaction);
    }

    public ResponseEntity<String> post(String url, String body, Map<String, String> headers, Transaction transaction) throws WebServicesException {
        return executeRequest(url, headers, body, HttpMethod.POST, transaction);
    }

    public ResponseEntity<String> put(String url, String body, Map<String, String> headers, Transaction transaction) throws WebServicesException {
        return executeRequest(url, headers, body, HttpMethod.PUT, transaction);
    }

    public ResponseEntity<String> delete(String url, Map<String, String> headers, Map<String, String> uriVariables, Transaction transaction) throws WebServicesException {
        URI uri = UriComponentsBuilder.fromUriString(url)
                .buildAndExpand(uriVariables != null ? uriVariables : Map.of())
                .toUri();
        return executeRequest(uri.toString(), headers, null, HttpMethod.DELETE, transaction);
    }

    private ResponseEntity<String> executeRequest(String url, Map<String, String> headers, String body, HttpMethod method, Transaction transaction) throws WebServicesException {
        try {
            configureSSL();
            HttpHeaders httpHeaders = createHeaders(headers);
            RestTemplate restTemplate = new RestTemplate();
            DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
            factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE); // Desactiva codificación automática
            restTemplate.setUriTemplateHandler(factory);
            HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);

            return restTemplate.exchange(url, method, entity, String.class);
        } catch (HttpClientErrorException ex) {
            String errorMsg = String.format("Error en la solicitud HTTP (4xx) al servicio %s: %s", url, ex.getStatusCode());
            throw new WebServicesException(transaction, errorMsg, ex, this);
        } catch (HttpServerErrorException ex) {
            String errorMsg = String.format("Error en el servidor (5xx) al servicio %s: %s", url, ex.getStatusCode());
            throw new WebServicesException(transaction, errorMsg, ex, this);
        } catch (ResourceAccessException ex) {
            String errorMsg = String.format("Error de acceso al recurso %s: Posible timeout o desconexión", url);
            throw new WebServicesException(transaction, errorMsg, ex, this);
        } catch (UnknownHostException ex) {
            String errorMsg = String.format("Error: No se pudo resolver el host del servicio %s", url);
            throw new WebServicesException(transaction, errorMsg, ex, this);
        } catch (RestClientException ex) {
            String errorMsg = String.format("Error en la comunicación con el servicio REST %s", url);
            throw new WebServicesException(transaction, errorMsg, ex, this);
        } catch (Exception ex) {
            String errorMsg = String.format("Error inesperado en la llamada REST al servicio %s", url);
            throw new WebServicesException(transaction, errorMsg, ex, this);
        }
    }

    private static void configureSSL() throws Exception {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new TrustAllHostnames());
    }

    private static class TrustAllCerts implements TrustManager, X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    }

    private static class TrustAllHostnames implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public static String encodeCredentials(String user, String pass) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("El usuario no puede ser nulo o vacío.");
        }
        if (pass == null || pass.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede ser nula o vacía.");
        }

        String credentials = user + ":" + pass;
        return new String(Base64.getEncoder().encode(credentials.getBytes()));
    }

    private HttpHeaders createHeaders(Map<String, String> additionalHeaders) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, "application/json"); // Configura el encabezado Accept
        if (additionalHeaders != null) {
            headers.setAll(additionalHeaders); // Agrega otros encabezados si existen
        }
        return headers;
    }

    public String encodeUrl(String url) {
        return UriComponentsBuilder
                .fromUriString(url)
                .encode()
                .toUriString();
    }
}