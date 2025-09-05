package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.repository.adapter;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.*;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.ParametersConfig;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class PollerAdapter implements IPollerPortOut {

    private final RestTemplate restTemplate;

    @Value("${poller.service.arp.url}")
    private String pollerServiceArpUrl;
    
    @Value("${poller.service.getestadocm.url}")
    private String pollerServiceGetACM;
   
    @Value("${poller.service.getconsultabanda.url}")
    private String pollerServiceConsultaBanda;
    
    
    @Override
    public InventarioPorClienteResponse obtenerInventarioPorCliente(InventarioPorClienteRequest request) throws Exception {

        try {

            HttpHeaders httpHeaders = new HttpHeaders();

            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<InventarioPorClienteRequest> entity = new HttpEntity<>(request, httpHeaders);

            ResponseEntity<InventarioPorClienteResponse> response = restTemplate.exchange(
                    ParametersConfig.getPropertyValue(Constantes.POLLER_SERVICE_URL, Transaction.startTransaction()),
                    HttpMethod.POST,
                    entity,
                    InventarioPorClienteResponse.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Error al consumir el servicio de Poller", e);

        }
    }
    
    @Override
    public List<ResponseArpPollerDto> consultarARP(String mac) {
        String finalUrl = pollerServiceArpUrl + mac;

        try {
  
        	ResponseArpDto apiResponse = restTemplate.getForObject(finalUrl, ResponseArpDto.class);
            
            if (apiResponse != null && apiResponse.getData() != null) {
                return apiResponse.getData();
            } else {
                return Collections.emptyList();
            }

        } catch (RestClientException e) {
            throw new RuntimeException("Error al consumir el servicio de Poller consultarARP para la MAC: " + mac, e);
        }
    }

	@Override
	public List<ResponseCmDataPollerDto> consultarCMData(String mac) throws Exception {
		String finalUrl = pollerServiceGetACM + mac;

        try {
  
        	ResponseCmDataDto apiResponse = restTemplate.getForObject(finalUrl, ResponseCmDataDto.class);
            
            if (apiResponse != null && apiResponse.getData() != null) {
                return apiResponse.getData();
            } else {
                return Collections.emptyList();
            }

        } catch (RestClientException e) {
            throw new RuntimeException("Error al consumir el servicio de Poller consultarCMData para la MAC: " + mac, e);
        }
	}

	@Override
	public List<ResponseGetWifiData> consultarCMBandas(String mac) throws Exception {
	    String finalUrl = pollerServiceConsultaBanda + mac;

	    try {
	     
	        ResponseWifiDataClient apiResponse = restTemplate.getForObject(finalUrl, ResponseWifiDataClient.class);
	        
	        if (apiResponse != null && apiResponse.getData() != null) {
	            // Devuelves la lista interna, que ahora coincide con la firma del método.
	            return apiResponse.getData();
	        } else {
	            return Collections.emptyList();
	        }

	    } catch (RestClientException e) {
	        throw new RuntimeException("Error al consumir el servicio de Poller consultarCMBandas para la MAC: " + mac, e);
	    }
	}

    @Override
    public List<ResponseNeighborStatusDto> obtenerVecinos(RequestNeighborStatusDto neighborStatusDto) throws Exception {
        return List.of();
    }
}
