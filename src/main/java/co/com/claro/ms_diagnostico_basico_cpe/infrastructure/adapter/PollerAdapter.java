package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.adapter;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteRequest;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.PollerPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PollerAdapter implements PollerPortOut {

    private final RestTemplate restTemplate;

    @Value("${poller.service.url}")
    private String pollerServiceUrl;



    @Override
    public InventarioPorClienteResponse obtenerInventarioPorCliente(InventarioPorClienteRequest request) throws Exception {

        try {

            HttpHeaders httpHeaders = new HttpHeaders();

            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<InventarioPorClienteRequest> entity = new HttpEntity<>(request, httpHeaders);

            ResponseEntity<InventarioPorClienteResponse> response = restTemplate.exchange(
                    pollerServiceUrl,
                    HttpMethod.POST,
                    entity,
                    InventarioPorClienteResponse.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Error al consumir el servicio de Poller", e);

        }
    }
}
