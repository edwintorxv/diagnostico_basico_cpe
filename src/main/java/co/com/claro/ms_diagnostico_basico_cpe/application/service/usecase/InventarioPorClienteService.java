package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;


import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteRequest;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.PollerPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventarioPorClienteService {

    private final PollerPortOut pollerPortOut;


    public InventarioPorClienteResponse consultarInventarioPorCliente(InventarioPorClienteRequest request) throws Exception{
        return pollerPortOut.obtenerInventarioPorCliente(request);
    }
}
