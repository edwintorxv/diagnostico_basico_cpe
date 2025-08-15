package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.utils.configuration;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.exceptions.WebServicesException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.net.ssl.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;

@Slf4j
@Component
public class UtilSoapClient {

    /**
     * Sobrecarga del método consumeWSDLService para que no exija el Map de headers.
     */
    public ResponseEntity<String> consumeWSDLService(
            String wsdl,
            String xmlRequest,
            Transaction transaction) throws Exception {

        // Llama al método principal indicando null en el mapa de encabezados
        return consumeWSDLService(wsdl, xmlRequest, null, transaction);
    }

    /**
     * Método principal para consumir un servicio SOAP con la opción de enviar encabezados.
     *
     * @param wsdl        URL del servicio SOAP.
     * @param xmlRequest  Cuerpo de la petición SOAP en formato XML.
     * @param headers     Encabezados adicionales que se deseen agregar (opcional).
     * @param transaction Información de la transacción para el manejo de logs y excepciones.
     * @return ResponseEntity con la respuesta convertida a JSON.
     * @throws Exception si ocurre algún error de conexión, parseo, o configuración SSL.
     */
    public ResponseEntity<String> consumeWSDLService(
            String wsdl,
            String xmlRequest,
            Map<String, String> headers,
            Transaction transaction) throws Exception {

        HttpURLConnection connection = null;
        String responseJson;

        try {
            // Configuración SSL (si se requiere omitir validaciones de certificados)
            configureSSL();

            // Construir la conexión con la URL y los headers
            connection = createHttpPostConnection(wsdl, headers);

            // Enviar cuerpo XML
            sendRequestBody(connection, xmlRequest);

            // Verificar código de respuesta
            int responseCode = connection.getResponseCode();
            if (responseCode >= 400) {
                String errorMsg = String.format(
                        "Error en la solicitud SOAP al servicio %s | Código %d | %s",
                        wsdl, responseCode, readErrorResponse(connection)
                );
                throw new WebServicesException(transaction, errorMsg, null, this);
            }

            // Parsear la respuesta exitosa a JSON
            responseJson = parseXmlResponse(connection);
            return new ResponseEntity<>(responseJson, HttpStatus.OK);

        } catch (MalformedURLException ex) {
            String errorMsg = String.format("URL inválida del servicio SOAP: %s", wsdl);
            throw new WebServicesException(transaction, errorMsg, ex, this);
        } catch (IOException ex) {
            String errorMsg = String.format("Error de conexión o de lectura/escritura con el servicio SOAP: %s", wsdl);
            throw new WebServicesException(transaction, errorMsg, ex, this);
        } catch (ParserConfigurationException | SAXException ex) {
            String errorMsg = String.format("Error al procesar la respuesta XML del servicio SOAP: %s", wsdl);
            throw new WebServicesException(transaction, errorMsg, ex, this);
        } catch (GeneralSecurityException ex) {
            String errorMsg = String.format("Error en la configuración SSL del servicio SOAP: %s", wsdl);
            throw new WebServicesException(transaction, errorMsg, ex, this);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Crea y configura la conexión HTTP (POST) para el llamado SOAP, aplicando
     * encabezados básicos y cualquier encabezado adicional.
     */
    private HttpURLConnection createHttpPostConnection(String wsdl, Map<String, String> headers) throws IOException {
        URL url = new URL(wsdl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Configura método y permite entrada/salida
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        // Establecemos encabezados obligatorios si no se han provisto
        if (headers == null || !headers.containsKey("Content-Type")) {
            connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        }
        if (headers == null || !headers.containsKey("SOAPAction")) {
            connection.setRequestProperty("SOAPAction", "");
        }

        // Aplica headers adicionales si vienen
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        return connection;
    }

    /**
     * Envía el cuerpo XML de la petición.
     */
    private void sendRequestBody(HttpURLConnection connection, String xmlRequest) throws IOException {
        try (OutputStream out = connection.getOutputStream()) {
            out.write(xmlRequest.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }

    /**
     * Lee la respuesta XML y la convierte en JSON.
     */
    private String parseXmlResponse(HttpURLConnection connection) throws Exception {
        try (InputStream inputStream = connection.getInputStream()) {
            String responseXML = convertStreamToString(inputStream);
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode jsonNode = xmlMapper.readTree(responseXML.getBytes(StandardCharsets.UTF_8));
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        }
    }

    /**
     * Lee la respuesta de error en caso de que el código sea >= 400,
     * e intenta extraer el faultstring.
     */
    private String readErrorResponse(HttpURLConnection connection) throws Exception {
        try (InputStream errorStream = connection.getErrorStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {

            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            return extractFaultString(responseBuilder.toString());
        }
    }

    /**
     * Convierte un InputStream en un String.
     */
    private String convertStreamToString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }

    /**
     * Configura un SSLContext que confía en todos los certificados.
     * Úsalo con precaución solo en entornos controlados.
     */
    private static void configureSSL() throws Exception {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new TrustAllHostnames());
    }

    /**
     * TrustManager que confía en todos los certificados.
     */
    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    }

    /**
     * HostnameVerifier que confía en cualquier nombre de host.
     */
    private static class TrustAllHostnames implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /**
     * Extrae la etiqueta <faultstring> de un XML de error SOAP.
     */
    private static String extractFaultString(String xmlResponse) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        try (InputStream inputStream = new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8))) {
            Document document = builder.parse(inputStream);
            return document.getElementsByTagName("faultstring").getLength() > 0
                    ? document.getElementsByTagName("faultstring").item(0).getTextContent()
                    : "faultstring no encontrado.";
        }
    }
}
