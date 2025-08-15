package co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteRequest;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteResponse;


public interface IPollerPortOut {

    InventarioPorClienteResponse obtenerInventarioPorCliente(InventarioPorClienteRequest request) throws Exception;

}