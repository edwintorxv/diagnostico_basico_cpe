package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.utils.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 3/02/2025
 */
public class UtilDto {
    /**
     * Inicializa un DTO estableciendo valores predeterminados en todos sus campos.
     * - Cadenas →""
     * - Números → 0
     * - Booleanos → false
     * - Listas y Sets → Colección vacía
     * - Mapas → Mapa vacío
     * - Objetos personalizados → Se instancia recursivamente
     *
     * @param dtoClass Clase del DTO a inicializar.
     * @param <T>      Tipo del DTO.
     * @return Instancia del DTO con sus valores inicializados.
     * @throws ReflectiveOperationException Si ocurre un error de reflexión.
     */
    public static <T> T initializeDto(Class<T> dtoClass) throws ReflectiveOperationException {
        T instance = dtoClass.getDeclaredConstructor().newInstance();

        for (Field field : dtoClass.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue; // Saltamos campos estáticos y finales
            }

            field.setAccessible(true); // Permite acceder a campos privados
            Object defaultValue = getDefaultValue(field.getType());
            field.set(instance, defaultValue);
        }

        return instance;
    }

    /**
     * Retorna un valor predeterminado basado en el tipo de dato del campo.
     *
     * @param type Clase del tipo de dato.
     * @return Valor inicial apropiado.
     * @throws ReflectiveOperationException Si ocurre un error al instanciar un objeto.
     */
    private static Object getDefaultValue(Class<?> type) throws ReflectiveOperationException {
        if (type == String.class) {
            return "";
        } else if (Number.class.isAssignableFrom(type) || type == int.class || type == long.class ||
                type == double.class || type == float.class || type == short.class || type == byte.class) {
            return 0;
        } else if (type == boolean.class || type == Boolean.class) {
            return false;
        } else if (List.class.isAssignableFrom(type)) {
            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(type)) {
            return new HashSet<>();
        } else if (Map.class.isAssignableFrom(type)) {
            return new HashMap<>();
        } else if (type.isEnum()) {
            return type.getEnumConstants().length > 0 ? type.getEnumConstants()[0] : null;
        } else if (!type.isPrimitive()) {
            return type.getDeclaredConstructor().newInstance(); // Instancia recursivamente objetos personalizados
        }
        return null; // Para otros tipos desconocidos
    }
}
