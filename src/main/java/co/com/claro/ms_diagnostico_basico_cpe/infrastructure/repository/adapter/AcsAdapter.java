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

        for (int i = 0; i < 2; i++) {
            try {
                String jsonPayload = objectMapper.writeValueAsString(dto);
                Map<String, String> hd = Map.of("Content-Type", MediaType.APPLICATION_JSON_VALUE);

                ResponseEntity<String> res = utilRestClient.post(url, jsonPayload, hd, Transaction.startTransaction());

                if (res.getStatusCode().is2xxSuccessful()) {
                    return UtilJson.mapResponseToDto(res, DeviceParamsResponse.class);
                }
            } catch (Exception ex) {
                if (i == 1) throw ex; // Si es el segundo intento, lanza la falla
                Thread.sleep(1000); // Espera 1 segundo antes de reintentar (lo que simularía tu debug)
            }
        }
        return null;
    }

    @Override
    public PingCpeResponseDto obtenerResultadoPingCPE(
            PingCpeRequestDto pingCpeRequestDto) throws Exception {

        final String url = ParametersConfig.getPropertyValue(
                Constantes.QUERY_ACS_WS_PING_CPE,
                Transaction.startTransaction()
        );
        try {
            String jsonPayload = objectMapper.writeValueAsString(pingCpeRequestDto);
            Map<String, String> headers = Map.of(
                    "Content-Type", MediaType.APPLICATION_JSON_VALUE
            );
            ResponseEntity<String> res = utilRestClient.post(
                    url,
                    jsonPayload,
                    headers,
                    Transaction.startTransaction()
            );
            return UtilJson.mapResponseToDto(res, PingCpeResponseDto.class);
        } catch (HttpStatusCodeException ex) {
            throw new Exception(
                    "Error HTTP en consulta a ACS. Status: " + ex.getStatusCode(), ex
            );
        } catch (Exception ex) {
            throw new Exception("Error inesperado al consultar ACS", ex);
        }
    }

    @Override
    public TraceCpeResponseDto obtenerResultadoTrace(TraceCpeRequestDto traceCpeRequestDto) throws Exception {
        final String url = ParametersConfig.getPropertyValue(Constantes.QUERY_ACS_WS_TRACE_CPE, Transaction.startTransaction());

        try {
            String jsonPayload = objectMapper.writeValueAsString(traceCpeRequestDto);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPayload, headers);

            // SELECCIONAMOS SOLO UN CLIENTE (RestTemplate es el estándar en Spring)
            ResponseEntity<TraceCpeResponseDto> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    TraceCpeResponseDto.class
            );

            return responseEntity.getBody();

        } catch (HttpStatusCodeException ex) {
            // Es vital loguear el cuerpo del error para saber qué dice el ACS (ej: 409 Conflict o 503 Busy)
            log.error("Error HTTP en TraceRoute: {}", ex.getResponseBodyAsString());
            throw new Exception("Error de comunicación con el servidor de diagnóstico (ACS).", ex);
        } catch (Exception ex) {
            log.error("Error inesperado en obtenerResultadoTrace", ex);
            throw new Exception("No se pudo completar la traza hacia el dispositivo.", ex);
        }
    }
}



