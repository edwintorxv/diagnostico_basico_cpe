package co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller;

import java.util.List;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteRequest;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseArpPollerDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseCmDataPollerDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseGetWifiData;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseWifiDataClient;


public interface IPollerPortOut {

    InventarioPorClienteResponse obtenerInventarioPorCliente(InventarioPorClienteRequest request) throws Exception;
    
    List<ResponseArpPollerDto> consultarARP(String mac) throws Exception;
    
    List<ResponseCmDataPollerDto> consultarCMData(String mac) throws Exception;
    
    List<ResponseGetWifiData> consultarCMBandas(String mac) throws Exception;
    
    
    

}