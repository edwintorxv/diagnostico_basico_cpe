package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.utils.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;


/**
 * Clase Util
 * <p>
 * Esta clase proporciona utilidades para manejar la serialización y deserialización de objetos JSON,
 * adaptando tipos especiales como `Instant`, `Optional` y proxies de Hibernate. También incluye
 * métodos para manipular estructuras JSON de manera flexible, como buscar propiedades anidadas
 * y convertir datos JSON a listas de objetos tipados.
 * <p>
 * Funcionalidades clave:
 * - Serialización personalizada de tipos complejos utilizando adaptadores de Gson.
 * - Validación de cadenas JSON para determinar si son válidas.
 * - Desenmascaramiento de proxies de Hibernate para evitar problemas al serializar.
 * - Métodos para búsqueda recursiva de propiedades dentro de JSON.
 * - Conversión de propiedades JSON en listas tipadas utilizando Jackson.
 * <p>
 * Esta clase es útil en aplicaciones que necesitan integrar servicios JSON complejos o que
 * utilizan Hibernate y necesitan manejar proxies con cuidado.
 * <p>
 * Dependencias clave:
 * - Gson: Para la serialización y deserialización principal.
 * - Jackson: Para manipulación avanzada de estructuras JSON.
 * - Hibérnate: Para soporte de proxies en serialización.
 * - SLF4J: Para el registro de logs.
 *
 * @autor <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 11/07/2024
 */
@Slf4j // Proporciona un logger para registrar mensajes en esta clase.
public class UtilJson {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Adaptador para manejar la serialización y deserialización de Instant en formato ISO-8601.
    private static final TypeAdapter<Instant> instantTypeAdapter = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Instant value) throws IOException {
            // Si el valor no es nulo, lo serializa como una cadena en formato ISO-8601.
            // Ejemplo: "2024-03-13T12:45:30.123Z"
            out.value(value != null ? value.toString() : null);
        }

        @Override
        public Instant read(JsonReader in) throws IOException {
            // Si el valor no es nulo y hay un dato disponible, lo parsea como un objeto Instant.
            return in != null && in.peek() != null ? Instant.parse(in.nextString()) : null;
        }
    };

    // Adaptador para manejar la serialización de objetos proxy de Hibernate.
    private static final TypeAdapter<Object> hibernateProxyAdapter = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Object value) throws IOException {
            // Si el objeto es un proxy de Hibernate, lo desenmascara obteniendo su instancia real.
            Object unproxiedValue = value instanceof HibernateProxy ? Hibernate.unproxy(value) : value;
            if (unproxiedValue != null) {
                // Serializa el objeto desenmascarado utilizando Gson.
                out.jsonValue(gson.toJson(unproxiedValue));
            } else {
                out.nullValue(); // Si el objeto és nulo, se serializa com null.
            }
        }

        @Override
        public Object read(JsonReader in) throws IOException {
            // HibernateProxy no admite deserialización, por lo que se lanza una excepción.
            throw new UnsupportedOperationException("Deserialization of HibernateProxy is not supported.");
        }
    };

    // Adaptador para manejar la serialización de objetos Optional.
    private static final TypeAdapter<Optional<?>> optionalTypeAdapter = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Optional<?> value) throws IOException {
            if (value != null && value.isPresent()) {
                // Si Optional tiene un valor, serializa su contenido.
                out.jsonValue(gson.toJson(value.get()));
            } else {
                out.nullValue(); // Si Optional está vacío, se serializa como null.
            }
        }

        @Override
        public Optional<?> read(JsonReader in) throws IOException {
            // La deserialización de Optional no es soportada, por lo que se lanza una excepción.
            throw new UnsupportedOperationException("Deserialization of Optional is not supported.");
        }
    };

    // Adaptador para manejar la serialización y deserialización de Charset.
    private static final TypeAdapter<Charset> charsetTypeAdapter = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Charset value) throws IOException {
            // Serializa el Charset como su nombre en formato de cadena (ejemplo: "UTF-8").
            out.value(value != null ? value.name() : null);
        }

        @Override
        public Charset read(JsonReader in) throws IOException {
            // Si hay un valor disponible, lo convierte en un objeto Charset usando su nombre.
            return in != null && in.peek() != null ? Charset.forName(in.nextString()) : null;
        }
    };

    // Adaptador para manejar la serialización de Throwable (Excepciones).
    private static final TypeAdapter<Throwable> throwableTypeAdapter = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Throwable value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            // Crea un objeto JSON para almacenar la información relevante de la excepción.
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", value.getMessage()); // Mensaje de la excepción.
            jsonObject.addProperty("exceptionType", value.getClass().getName()); // Tipo de excepción.

            // Serializa la pila de llamadas como un array de strings.
            JsonArray stackTraceArray = new JsonArray();
            for (StackTraceElement element : value.getStackTrace()) {
                stackTraceArray.add(element.toString());
            }
            jsonObject.add("stackTrace", stackTraceArray);

            // Serializa el objeto JSON resultante.
            out.jsonValue(jsonObject.toString());
        }

        @Override
        public Throwable read(JsonReader in) throws IOException {
            // No es recomendable deserializar excepciones, por lo que se lanza una excepción.
            throw new UnsupportedOperationException("Deserialization of Throwable is not supported.");
        }
    };

    // Configuración de Gson con los adaptadores personalizados y opciones generales.
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, instantTypeAdapter) // Registro del adaptador de Instant.
            .registerTypeHierarchyAdapter(HibernateProxy.class, hibernateProxyAdapter) // Registro del adaptador de HibernateProxy.
            .registerTypeAdapter(Optional.class, optionalTypeAdapter) // Registro del adaptador de Optional.
            .registerTypeAdapter(Charset.class, charsetTypeAdapter) // Registro del adaptador de Charset.
            .registerTypeAdapter(Throwable.class, throwableTypeAdapter) // Registro del adaptador de Throwable.
            .serializeNulls() // Permite incluir valores nulos en la serialización.
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY) // Mantiene los nombres originales de los campos.
            .disableHtmlEscaping() // Desactiva el escape automático de caracteres Unicode.
            .create(); // Crea la instancia de Gson.


    /**
     * Convierte un objeto a su representación JSON, decodificando cualquier string Unicode codificado.
     *
     * @param object El objeto a convertir.
     * @return La cadena JSON resultante.
     */
    public static String toJson(Object object) {
        try {
            // 0) Si es un ResponseEntity<?>
            if (object instanceof ResponseEntity<?>) {
                ResponseEntity<?> resp = (ResponseEntity<?>) object;
                JsonObject root = new JsonObject();
                // Status
                root.addProperty("status", resp.getStatusCode().toString());
                // Headers (usamos toSingleValueMap para simplificar)
                root.add("headers", gson.toJsonTree(resp.getHeaders().toSingleValueMap()));
                // Body
                Object body = resp.getBody();
                if (body instanceof String) {
                    String bodyStr = (String) body;
                    // Quitar comillas envolventes si las hubiera
                    if (bodyStr.startsWith("\"") && bodyStr.endsWith("\"")) {
                        bodyStr = bodyStr.substring(1, bodyStr.length() - 1);
                    }
                    if (isValidJson(bodyStr)) {
                        root.add("body", JsonParser.parseString(bodyStr));
                    } else {
                        root.addProperty("body", bodyStr);
                    }
                } else {
                    // No es String: serializamos y parseamos para JsonElement
                    root.add("body", JsonParser.parseString(gson.toJson(body)));
                }
                return root.toString();
            }

            // 1) Si es una colección, procesamos elemento a elemento
            if (object instanceof Collection<?>) {
                JsonArray array = new JsonArray();
                for (Object item : (Collection<?>) object) {
                    if (item instanceof String && isValidJson((String) item)) {
                        array.add(JsonParser.parseString((String) item));
                    } else {
                        array.add(JsonParser.parseString(gson.toJson(item)));
                    }
                }
                return array.toString();
            }

            // 2) Si ya es un String, lo tratamos como JSON crudo o texto plano
            if (object instanceof String) {
                String jsonString = (String) object;
                if (jsonString.startsWith("\"") && jsonString.endsWith("\"")) {
                    jsonString = jsonString.substring(1, jsonString.length() - 1);
                }
                if (isValidJson(jsonString)) {
                    return JsonParser.parseString(jsonString).toString();
                }
                return jsonString;
            }

            // 3) Si es un proxy de Hibernate, desenmascarar
            if (object instanceof HibernateProxy) {
                object = Hibernate.unproxy(object);
            }

            // 4) Resto de objetos: serializar con tu Gson configurado
            return gson.toJson(object);

        } catch (JsonSyntaxException e) {
            log.error("Error de sintaxis JSON: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error inesperado al convertir a JSON: {}", e.getMessage(), e);
        }
        return "{}";
    }


    /**
     * Recorre recursivamente un JSON y decodifica cualquier cadena con caracteres Unicode codificados.
     *
     * @param jsonElement El elemento JSON a procesar.
     * @return Un nuevo JsonElement con todas las cadenas decodificadas.
     */
    private static JsonElement decodeUnicodeStrings(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject decodedObject = new JsonObject();

            jsonObject.entrySet().forEach(entry ->
                    decodedObject.add(entry.getKey(), decodeUnicodeStrings(entry.getValue()))
            );

            return decodedObject;
        } else if (jsonElement.isJsonArray()) {
            JsonArray decodedArray = new JsonArray();
            jsonElement.getAsJsonArray().forEach(element ->
                    decodedArray.add(decodeUnicodeStrings(element))
            );
            return decodedArray;
        } else if (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
            return new JsonPrimitive(StringEscapeUtils.unescapeJson(jsonElement.getAsString()));
        }

        return jsonElement;
    }


    /**
     * Verifica si un texto es un JSON válido.
     *
     * @param text El texto a verificar.
     * @return true si el texto es JSON válido, false en caso contrario.
     */
    private static boolean isValidJson(String text) {
        try {
            gson.fromJson(text, Object.class); // Intenta deserializar el texto como JSON.
            return true;
        } catch (JsonSyntaxException e) {
            return false; // Retorna false si ocurre un error de sintaxis JSON.
        }
    }

    /**
     * Busca una propiedad en un JSON y devuelve su valor.
     *
     * @param responseJson La respuesta JSON en formato ResponseEntity<String>.
     * @param propertyName El nombre de la propiedad a buscar.
     * @return El valor de la propiedad si se encuentra, o null si no.
     */
    public static Object getJsonPropertyValue(ResponseEntity<String> responseJson, String propertyName) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseJson.getBody()); // Lee el JSON como un árbol de nodos.
        return searchPropertyRecursivelyInJsonNode(rootNode, propertyName); // Llama al método recursivo para buscar la propiedad.
    }

    /**
     * Método recursivo para buscar una propiedad en cualquier nivel del JSON.
     *
     * @param currentNode  El nodo actual del JSON.
     * @param propertyName El nombre de la propiedad a buscar.
     * @return El valor de la propiedad si se encuentra, o null si no.
     */
    public static Object searchPropertyRecursivelyInJsonNode(JsonNode currentNode, String propertyName) {
        if (currentNode == null) {
            return null; // Retorna null si el nodo actual es nulo.
        }

        if (currentNode.isObject()) {
            // Si el nodo actual es un objeto, obtenemos sus nombres de campo.
            Iterator<String> fieldNames = currentNode.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                JsonNode valueNode = currentNode.get(key);

                // Si encontramos la clave buscada, devolvemos su valor.
                if (key.equalsIgnoreCase(propertyName)) {
                    if (valueNode.isTextual()) {
                        // Si el valor es un String, devuelve el texto sin modificar.
                        return valueNode.textValue();
                    }
                    return valueNode; // Devuelve el nodo para otros tipos.
                }

                // Realiza la búsqueda recursiva en el subnodo actual.
                Object result = searchPropertyRecursivelyInJsonNode(valueNode, propertyName);
                if (result != null) {
                    return result;
                }
            }
        } else if (currentNode.isArray()) {
            // Si el nodo actual es un array, busca recursivamente en cada elemento.
            for (JsonNode elementNode : currentNode) {
                Object result = searchPropertyRecursivelyInJsonNode(elementNode, propertyName);
                if (result != null) {
                    return result;
                }
            }
        }

        return null; // Retorna null si no encuentra la propiedad.
    }


    /**
     * Convierte una cadena JSON o ResponseEntity que contiene una lista JSON directamente a una lista de objetos DTO.
     *
     * @param input    JSON en formato String o ResponseEntity<String>
     * @param dtoClass Clase del DTO objetivo
     * @param <T>      Tipo del DTO
     * @return Lista de objetos mapeados desde el JSON
     * @throws Exception Si hay errores en el parseo o tipo de dato inválido
     */
    public static <T> List<T> extractJsonAsList(Object input, Class<T> dtoClass) throws Exception {
        String json;

        // Determinar si la entrada es ResponseEntity o String
        if (input instanceof ResponseEntity<?>) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) input;
            json = responseEntity.getBody().toString();
        } else if (input instanceof String) {
            json = (String) input;
            // Limpieza por si hay caracteres escapados
            json = json.replace("\\", "").trim();
            if (json.startsWith("\"") && json.endsWith("\"")) {
                json = json.substring(1, json.length() - 1);
                json = json.replace("\\\"", "\""); // Desescapar
            }
        } else {
            throw new IllegalArgumentException("El parámetro 'input' debe ser ResponseEntity<String> o String.");
        }

        // Validar si es un array
        if (!json.trim().startsWith("[")) {
            throw new IllegalArgumentException("La entrada no parece ser un JSON array.");
        }

        // Deserializar el array directamente a una lista de DTOs
        return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, dtoClass));
    }


    /**
     * Método genérico para extraer una lista de objetos DTO desde una clave específica en un JSON.
     *
     * @param input    Puede ser un ResponseEntity<String> o un String con el JSON de entrada.
     * @param key      La clave que contiene los objetos a extraer.
     * @param dtoClass La clase del DTO a mapear.
     * @param <T>      El tipo genérico del DTO.
     * @return Una lista de objetos del tipo especificado o una lista vacía si no se encuentra la clave.
     * @throws Exception Si ocurre algún error durante el procesamiento.
     */
    public static <T> List<T> extractJsonAsList(Object input, String key, Class<T> dtoClass) throws Exception {
        JsonNode rootNode;

        // Determinar si la entrada es ResponseEntity o String
        if (input instanceof ResponseEntity<?>) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) input;
            rootNode = objectMapper.readTree(responseEntity.getBody().toString());
        } else if (input instanceof String) {
            rootNode = objectMapper.readTree((String) input);
        } else {
            throw new IllegalArgumentException("El parámetro 'input' debe ser ResponseEntity<String> o String.");
        }

        // Buscar la clave especificada (de manera recursiva)
        JsonNode targetNode = rootNode.findValue(key);

        if (targetNode == null) {
            return Collections.emptyList(); // Si la clave no existe, retornar una lista vacía.
        }

        if (targetNode.isArray()) {
            // Si la clave es un array, mapear cada elemento al DTO especificado.
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, dtoClass);
            return objectMapper.convertValue(targetNode, listType);
        }

        // Si la clave contiene un valor único, convertirlo al tipo DTO y retornar en una lista.
        T singleValue = objectMapper.convertValue(targetNode, dtoClass);
        return Collections.singletonList(singleValue);
    }

    public static <T> List<T> extractJsonAsList(Object input, String keyArray, String keyNode, Class<T> dtoClass) throws Exception {
        JsonNode rootNode;

        if (input instanceof ResponseEntity<?>) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) input;
            rootNode = objectMapper.readTree(responseEntity.getBody().toString());
        } else if (input instanceof String) {
            rootNode = objectMapper.readTree((String) input);
        } else {
            throw new IllegalArgumentException("El parámetro 'input' debe ser ResponseEntity<String> o String.");
        }

        JsonNode targetNode = rootNode.findValue(keyArray);

        if (targetNode == null || !targetNode.isArray()) {
            return Collections.emptyList(); // Retorna una lista vacía si no encuentra la clave o no es un array.
        }

        if (targetNode.isArray()) {
            // Si el array contiene nodos con keyNode, mapear al DTO interno
            List<T> results = new ArrayList<>();
            for (JsonNode element : targetNode) {
                // Encontrar la clave insensible a mayúsculas/minúsculas
                JsonNode jsonNode = null;

                // Iterar sobre las claves del nodo
                Iterator<String> fieldNames = element.fieldNames(); // Devuelve un Iterator
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    if (fieldName.equalsIgnoreCase(keyNode)) {
                        jsonNode = element.get(fieldName);
                        break;
                    }
                }

                if (jsonNode != null) {
                    T dto = objectMapper.treeToValue(jsonNode, dtoClass);
                    results.add(dto);
                }
            }

            return results;
        }

        return Collections.emptyList();
    }


    /**
     * Valida si el cuerpo de un ResponseEntity contiene un array no vacío en una clave específica.
     *
     * @param responseEntity La respuesta HTTP con cuerpo en formato JSON.
     * @param key            La clave en el JSON que se quiere validar.
     * @return {@code true} si la clave existe y su valor es un array no vacío, {@code false} en cualquier otro caso.
     * @throws Exception Si ocurre un error al procesar el JSON.
     */
    public static boolean containsNonEmptyArray(ResponseEntity<String> responseEntity, String key) throws Exception {
        // Extraer el cuerpo de la respuesta
        String responseBody = responseEntity.getBody();

        if (responseBody == null || responseBody.isEmpty()) {
            return false; // Respuesta vacía o nula
        }

        // Crear un ObjectMapper para procesar el JSON
        JsonNode rootNode = objectMapper.readTree(responseBody);

        // Verificar si el JSON contiene la clave y si el valor asociado es un array no vacío
        JsonNode targetNode = rootNode.get(key);
        return targetNode != null && targetNode.isArray() && !targetNode.isEmpty();
    }

    /**
     * Extrae una lista de objetos de una respuesta JSON anidada en el campo tagName.
     *
     * @param responseEntity La respuesta JSON encapsulada en un ResponseEntity<String>.
     * @param tagName        El nombre de la etiqueta que contiene los objetos JSON.
     * @param dtoClass       La clase a la que se mapearán los objetos JSON.
     * @param <T>            El tipo genérico del DTO.
     * @return Una lista de objetos del tipo especificado.
     * @throws Exception Si ocurre algún error al procesar el JSON.
     */
    public static <T> List<T> extractJsonFromSoap(ResponseEntity<String> responseEntity, String tagName, Class<T> dtoClass) throws Exception {
        if (responseEntity == null || responseEntity.getBody() == null || responseEntity.getBody().isEmpty()) {
            throw new IllegalArgumentException("La respuesta no puede ser nula o vacía.");
        }

        String jsonResponse = responseEntity.getBody();

        // Leer el JSON completo
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Buscar la clave especificada (de manera recursiva)
        JsonNode targetNode = rootNode.findValue(tagName);

        if (targetNode.isMissingNode() || !targetNode.isArray()) {
            return Collections.emptyList(); // Si no existe el nodo o no es un array, retornar lista vacía
        }

        // Lista para almacenar los resultados
        List<T> resultList = new ArrayList<>();

        // Iterar sobre el array y mapear cada string JSON al DTO
        for (JsonNode jsonNode : targetNode) {
            String jsonString = jsonNode.asText(); // Extraer el string JSON
            T dto = objectMapper.readValue(jsonString, dtoClass); // Mapear al DTO
            resultList.add(dto);
        }

        return resultList;
    }

    /**
     * Extrae una lista de objetos desde una respuesta JSON anidada dentro de un nodo padre y un campo específico.
     * <p>
     * El método busca primero el nodo padre indicado por {@code parentTagName}, luego dentro de este, el campo {@code tagName}.
     * El contenido de este campo puede ser:
     * <ul>
     *     <li>Un array de objetos JSON.</li>
     *     <li>Un string que contenga un objeto JSON o un array de objetos JSON.</li>
     * </ul>
     * El contenido encontrado se mapea a instancias de la clase {@code dtoClass}.
     * <p>
     * Si el nodo padre o el campo especificado no existen, se retorna una lista vacía.
     *
     * @param responseEntity La respuesta HTTP que contiene el JSON como cadena dentro del body.
     * @param parentTagName  El nombre del nodo padre donde se encuentra el campo objetivo.
     * @param tagName        El nombre del campo que contiene el objeto o arreglo JSON a mapear.
     * @param dtoClass       La clase destino a la que se desea mapear los objetos JSON.
     * @param <T>            El tipo genérico correspondiente al DTO de destino.
     * @return Una lista de objetos del tipo {@code T} extraídos del JSON.
     * @throws IllegalArgumentException Si la respuesta es nula o vacía.
     * @throws Exception                Si ocurre algún error al procesar o mapear el contenido JSON.
     */
    public static <T> List<T> extractJsonFromSoap(ResponseEntity<String> responseEntity, String parentTagName, String tagName, Class<T> dtoClass) throws Exception {
        if (responseEntity == null || responseEntity.getBody() == null || responseEntity.getBody().isEmpty()) {
            throw new IllegalArgumentException("La respuesta no puede ser nula o vacía.");
        }

        String jsonResponse = responseEntity.getBody();

        // Leer el JSON completo
        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // Buscar el nodo padre
        JsonNode parentNode = rootNode.findValue(parentTagName);
        if (parentNode == null || parentNode.isMissingNode()) {
            return Collections.emptyList(); // Retornar lista vacía si el nodo padre no existe
        }

        // Buscar el nodo dentro del nodo padre
        JsonNode targetNode = parentNode.get(tagName);
        if (targetNode == null || targetNode.isMissingNode()) {
            return Collections.emptyList(); // Retornar lista vacía si el nodo no existe
        }

        // Lista para almacenar los resultados
        List<T> resultList = new ArrayList<>();

        // Soporta array o string JSON que representa un objeto o array
        if (targetNode.isArray()) {
            for (JsonNode jsonNode : targetNode) {
                String jsonString = jsonNode.asText();
                T dto = objectMapper.readValue(jsonString, dtoClass);
                resultList.add(dto);
            }
        } else {
            String jsonString = targetNode.asText();
            JsonNode parsedJson = objectMapper.readTree(jsonString);

            if (parsedJson.isArray()) {
                for (JsonNode node : parsedJson) {
                    T dto = objectMapper.treeToValue(node, dtoClass);
                    resultList.add(dto);
                }
            } else if (parsedJson.isObject()) {
                T dto = objectMapper.treeToValue(parsedJson, dtoClass);
                resultList.add(dto);
            }
        }

        return resultList;
    }

    /**
     * Mapea el cuerpo de un ResponseEntity a un objeto DTO especificado.
     *
     * @param responseEntity El ResponseEntity que contiene el JSON de respuesta.
     * @param dtoClass       La clase del DTO al que se debe mapear el JSON.
     * @param <T>            El tipo genérico del DTO.
     * @return Una instancia del DTO mapeado con los datos del JSON.
     * @throws IOException Si ocurre un error al procesar el JSON.
     */
    public static <T> T mapResponseToDto(ResponseEntity<String> responseEntity, Class<T> dtoClass) throws IOException {
        if (responseEntity == null || responseEntity.getBody() == null || responseEntity.getBody().isEmpty()) {
            throw new IllegalArgumentException("El ResponseEntity no puede ser nulo ni contener un cuerpo vacío.");
        }

        try {
            // Deserializa el JSON del cuerpo de la respuesta al tipo especificado.
            return objectMapper.readValue(responseEntity.getBody(), dtoClass);
        } catch (Exception e) {
            log.error("Error al mapear el JSON al DTO: {}", e.getMessage(), e);
            throw new IOException("No se pudo mapear el JSON al DTO especificado.", e);
        }
    }

    /**
     * Mapea una clave específica dentro del JSON del ResponseEntity a un objeto DTO.
     *
     * @param responseEntity El ResponseEntity que contiene el JSON de respuesta.
     * @param key            La clave dentro del JSON que se desea mapear.
     * @param dtoClass       La clase del DTO al que se debe mapear el JSON.
     * @param <T>            El tipo genérico del DTO.
     * @return Una instancia del DTO mapeado con los datos de la clave especificada.
     * @throws IOException Si ocurre un error al procesar el JSON.
     */
    public static <T> T mapResponseToDto(ResponseEntity<String> responseEntity, String key, Class<T> dtoClass) throws IOException {
        if (responseEntity == null || responseEntity.getBody() == null || responseEntity.getBody().isEmpty()) {
            throw new IllegalArgumentException("El ResponseEntity no puede ser nulo ni contener un cuerpo vacío.");
        }

        try {
            JsonNode rootNode = objectMapper.readTree(responseEntity.getBody()); // Convertir el JSON en árbol de nodos.
            JsonNode targetNode = rootNode.findValue(key); // Buscar el nodo correspondiente a la clave.

            if (targetNode == null || targetNode.isMissingNode()) {
                throw new IllegalArgumentException("No se encontró la clave '" + key + "' en el JSON de la respuesta.");
            }

            // Convertir el nodo JSON al DTO especificado.
            return objectMapper.treeToValue(targetNode, dtoClass);
        } catch (Exception e) {
            log.error("Error al mapear el JSON de la clave '{}' al DTO: {}", key, e.getMessage(), e);
            throw new IOException("No se pudo mapear el JSON de la clave '" + key + "' al DTO especificado.", e);
        }
    }


}
