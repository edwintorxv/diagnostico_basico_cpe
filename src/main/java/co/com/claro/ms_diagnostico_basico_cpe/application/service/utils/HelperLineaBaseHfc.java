package co.com.claro.ms_diagnostico_basico_cpe.application.service.utils;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.*;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class HelperLineaBaseHfc {

    private final IPollerPortOut pollerPortOut;


    public ResponseCmDataPollerDto responseCmDataPollerDto(String macAddress) throws Exception {
        List<ResponseCmDataPollerDto> getCmData = pollerPortOut.consultarCMData(macAddress);
        ResponseCmDataPollerDto dataCableModem;
        dataCableModem = getCmData.get(0);
        return dataCableModem;
    }

    public List<NeighborStatusDto> obtenerListadoVecinos(ResponseCmDataPollerDto dataCableModem) throws Exception {
        RequestNeighborStatusDto neighborStatusDto = new RequestNeighborStatusDto();
        neighborStatusDto.setCableModemDto(dataCableModem);
        neighborStatusDto.setSamples(30);
        neighborStatusDto.setGeodistanceQuery(false);
        ResponseNeighborStatusDto responseNeighborStatusDto = pollerPortOut.obtenerVecinos(neighborStatusDto);
        return responseNeighborStatusDto.getLstNeighbors();
    }

    public List<ProvisioningDataDto> aprovisionamientoCliente(ResponseCmDataPollerDto dataCableModem) throws Exception {
        RequestProvisioningDataDto requestProvisioningDataDto = new RequestProvisioningDataDto();
        requestProvisioningDataDto.setCableModemDto(dataCableModem);
        ResponseProvisioningDataDto responseProvisioningDataDto = pollerPortOut.obtenerAprovisionamiento(requestProvisioningDataDto);
        return responseProvisioningDataDto.getLstProvisioningDataDto();
    }

    public List<CableModemDataDto> datosCableModemPorModelo(ResponseCmDataPollerDto dataCableModem) throws Exception {
        RequestCableModemDataDto requestCableModemDataDto = new RequestCableModemDataDto();
        requestCableModemDataDto.setModelo(dataCableModem.getModel());
        ResponseCableModemDataDto responseCableModemDataDto = pollerPortOut.obtenerDataCableModem(requestCableModemDataDto);
        return responseCableModemDataDto.getLstCableModemDto();
    }


}
