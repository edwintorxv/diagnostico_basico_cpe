package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.adapter;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceStatusResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcsAdapter implements IAcsPortOut {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${acs.service.deviceStatus}")
    private String acsServiceDeviceStatus;

    @Value("${acs.service.deviceParameters}")
    private String acsServiceDeviceParameters;

    @Override
    public DeviceStatusResponse obtenerEstadoPorSerial(String serial) throws Exception {

        try {
            String url = acsServiceDeviceStatus + "?serial=" + serial;

            ResponseEntity<DeviceStatusResponse> response =
                    restTemplate.getForEntity(url, DeviceStatusResponse.class);

            return response.getBody();

        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public DeviceParamsResponse obtenerParametrosPorDispositivo(DeviceParamsDto dto) throws Exception {
        final String url = acsServiceDeviceParameters;

        try {
            String jsonPayload = objectMapper.writeValueAsString(dto);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<DeviceParamsResponse> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    DeviceParamsResponse.class
            );

            DeviceParamsResponse body = responseEntity.getBody();

            return body;

        } catch (HttpStatusCodeException ex) {
            throw new Exception("Error HTTP en consulta a ACS", ex);

        } catch (Exception ex) {
            throw new Exception("Error inesperado al consultar ACS", ex);
        }
    }
}



