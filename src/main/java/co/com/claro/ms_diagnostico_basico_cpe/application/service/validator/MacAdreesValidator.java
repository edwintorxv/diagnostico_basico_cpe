package co.com.claro.ms_diagnostico_basico_cpe.application.service.validator;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest.configuration.properties.WifiChannelProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MacAdreesValidator {

    private final WifiChannelProperties wifiChannelProperties;

    public MacAdreesValidator(WifiChannelProperties wifiChannelProperties) {
        this.wifiChannelProperties = wifiChannelProperties;
    }


    public List<String> macAddressDetectadas(DeviceParamsResponse response, String keyOrTree) {

        if (response == null || response.getData() == null) {
            return new ArrayList<>();
        }

        // 1. Buscar todos los parámetros que sean "MACAddress" o "PhysAddress"
        List<String> macsCrudas = response.getData().stream()
                .flatMap(d -> d.getParams().stream())
                .filter(p -> p.getName().toLowerCase().contains("macaddress")
                        || p.getName().toLowerCase().contains("physaddress"))
                .map(p -> p.getValue())
                .collect(Collectors.toList());

        // 2. Hacer split de cada valor por ":" y acumular en una lista
        List<String> macsProcesadas = new ArrayList<>();
        for (String mac : macsCrudas) {
            if (mac != null && !mac.isBlank()) {
                macsProcesadas.addAll(Arrays.asList(mac.replace(":", "").toUpperCase()));
            }
        }

        return macsProcesadas;
    }
}
