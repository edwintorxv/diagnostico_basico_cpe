package co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.*;

import java.util.List;


public interface IPollerPortOut {

    InventarioPorClienteResponse obtenerInventarioPorCliente(InventarioPorClienteRequest request) throws Exception;

    List<ResponseArpPollerDto> consultarARP(String mac) throws Exception;

    List<ResponseCmDataPollerDto> consultarCMData(String mac) throws Exception;

    List<ResponseGetWifiData> consultarCMBandas(String mac) throws Exception;

    ResponseNeighborStatusDto obtenerVecinos(RequestNeighborStatusDto neighborStatusDto) throws Exception;

    ResponseProvisioningDataDto obtenerAprovisionamiento(RequestProvisioningDataDto provisioningDataDto) throws Exception;

    ResponseCableModemDataDto obtenerDataCableModem(RequestCableModemDataDto requestCableModemDataDto) throws Exception;

    ResponseRealTimeMeasurementDto obetnerNiveles(RequestRealTimeMeasurementDto requestRealTimeMeasurementDto);


}