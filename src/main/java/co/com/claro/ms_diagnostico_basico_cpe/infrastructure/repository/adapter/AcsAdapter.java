package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.repository.adapter;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.*;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.ParametersConfig;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.utils.configuration.UtilJson;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.utils.configuration.UtilRestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcsAdapter implements IAcsPortOut {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UtilRestClient utilRestClient;

    @Override
    public DeviceStatusResponse obtenerEstadoPorSerial(String serial) throws Exception {

        try {
            String url = ParametersConfig.getPropertyValue(Constantes.ACS_SERVICE_DEVICESTATUS, Transaction.startTransaction()) + "?serial=" + serial;

            ResponseEntity<DeviceStatusResponse> response =
                    restTemplate.getForEntity(url, DeviceStatusResponse.class);

            return response.getBody();

        } catch (Exception e) {

        }
        return null;
    }

    @Override
    public DeviceParamsResponse obtenerParametrosPorDispositivo(DeviceParamsDto dto) throws Exception {
        final String url = ParametersConfig.getPropertyValue(Constantes.ACS_SERVICE_DEVICEPARAMETERS, Transaction.startTransaction());

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

            Map<String, String> hd = Map.of("Content-Type", MediaType.APPLICATION_JSON_VALUE);

            ResponseEntity<String> res = utilRestClient.post(url,jsonPayload,hd,Transaction.startTransaction());
            UtilJson.toJson(res);
            body = UtilJson.mapResponseToDto(res,DeviceParamsResponse.class);

            return body;

        } catch (HttpStatusCodeException ex) {
            throw new Exception("Error HTTP en consulta a ACS", ex);

        } catch (Exception ex) {
            throw new Exception("Error inesperado al consultar ACS", ex);
        }
    }
}



