package co.com.claro.ms_diagnostico_basico_cpe.application.service.validator;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest.configuration.properties.WifiChannelProperties;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CanalWifiValidator {

    private final WifiChannelProperties wifiChannelProperties;

    public CanalWifiValidator(WifiChannelProperties wifiChannelProperties) {
        this.wifiChannelProperties = wifiChannelProperties;
    }


    public boolean validar(DeviceParamsResponse response, String keyOrTree) {
        // 1. Obtener desde properties la lista de parámetros que aplican a este modelo
        List<String> parametros = wifiChannelProperties.getChannelsForKey(keyOrTree.toLowerCase());
        if (parametros == null || parametros.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron parámetros configurados para: " + keyOrTree);
        }

        // 2. Buscar los dos parámetros (2.4GHz y 5GHz)
        String param24 = parametros.stream()
                .filter(p -> p.contains("WLANConfiguration.1.Status") || p.contains("WiFi.Radio.1.Status")
                        || p.contains("AccessPoint.1.Status") || p.contains("WIFI.Radio.1.Enable"))
                .findFirst()
                .orElse(null);

        String param5 = parametros.stream()
                .filter(p -> p.contains("WLANConfiguration.5.Status") || p.contains("WiFi.Radio.2.Status")
                        || p.contains("WiFi.AccessPoint.5.Status") || p.contains("WIFI.Radio.2.Enable"))
                .findFirst()
                .orElse(null);

        // 3. Extraer estado de cada uno
        String estado24 = extraerEstado(response, param24);
        String estado5 = extraerEstado(response, param5);

        // 4. Validar que al menos uno esté encendido
        return estaEncendido(estado24) && estaEncendido(estado5);
    }

    private String extraerEstado(DeviceParamsResponse response, String nombre) {
        if (nombre == null) return null;

        return response.getData().stream()
                .flatMap(d -> d.getParams().stream())
                .filter(p -> p.getName().equalsIgnoreCase(nombre))
                .map(p -> p.getValue())
                .findFirst()
                .orElse(null);
    }

    private boolean estaEncendido(String estado) {
        if (estado == null) return false;

        switch (estado.toLowerCase()) {
            case "1":
            case "up":
            case "enable":
            case "enabled":
                return true;
            default:
                return false;
        }
    }

}
