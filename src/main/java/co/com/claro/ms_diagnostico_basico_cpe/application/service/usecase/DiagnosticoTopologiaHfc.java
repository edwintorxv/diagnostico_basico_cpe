package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.DiagnosticoTopologiaHfcStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.TopologiaHfcConMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.TopologiaHfcSinMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.utils.HelperLineaBaseHfc;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.utils.HelperMesh;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoHfcLineaBaseDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoHfcLineaBaseResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.*;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico.IDiagnosticoHFCPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.ParametersConfig;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class DiagnosticoTopologiaHfc implements IDiagnosticoHFCPortIn {

    private final IAcsPortOut acsPortOut;
    private final IPollerPortOut pollerPortOut;

    private final TopologiaHfcSinMeshStrategy hfcSinMeshsStrategy;
    private final TopologiaHfcConMeshStrategy hfcConMeshsStrategy;
    private final InventarioPoller inventarioPoller;

    private final HelperLineaBaseHfc helperLineaBaseHfc;

    @Override
    public DiagnosticoResponse diagnosticoTopologiaHfc(String cuentaCliente) throws Exception {

        Transaction transaction = Transaction.startTransaction();
        cuentaCliente = HelperMesh.formatCuentaCliente(cuentaCliente);

        InventarioPorTopoligiaDto inventarioTopologiaHfc =
                inventarioPoller.consultarInventario(cuentaCliente, "hfc");

        if (inventarioTopologiaHfc == null || inventarioTopologiaHfc.getInventarioCPE() == null) {
            return new DiagnosticoResponse(
                    "OK",
                    ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                    List.of(new DiagnosticoDto(
                            cuentaCliente,
                            ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO, transaction),
                            ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, transaction)
                                    .replace("{}", cuentaCliente)
                    ))
            );
        }

        //Validar si el cable Modem esta conectado
        String formatearMac = HelperMesh.formatMacAddress(inventarioTopologiaHfc.getInventarioCPE().getSerialMac());
        List<ResponseCmDataPollerDto> getCmData = pollerPortOut.consultarCMData(formatearMac);

        if (getCmData == null || getCmData.isEmpty()) {
            return HelperMesh.diagnostico(cuentaCliente,
            		Constantes.HFC_NO_ONLINE_CODIGO,
            		Constantes.HFC_NO_ONLINE_DESCRIPCION);
        }

        ResponseCmDataPollerDto getEstadoBandas = (ResponseCmDataPollerDto) getCmData.get(0);

        if ("false".equalsIgnoreCase(getEstadoBandas.getAlive())) {
            return HelperMesh.diagnostico(cuentaCliente,
                    ParametersConfig.getPropertyValue(Constantes.HFC_NO_ONLINE_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.HFC_NO_ONLINE_DESCRIPCION, transaction));

        }

        DiagnosticoTopologiaHfcStrategy strategy =
                (inventarioTopologiaHfc.getLstinventarioMesh() == null || inventarioTopologiaHfc.getLstinventarioMesh().isEmpty())
                        ? hfcSinMeshsStrategy
                        : hfcConMeshsStrategy;

        return strategy.diagnosticar(inventarioTopologiaHfc, pollerPortOut, acsPortOut);

    }

    @Override
    public DiagnosticoHfcLineaBaseResponse diagnosticoLineaBaseHfc(String cuentaCliente) throws Exception {

        Transaction transaction = Transaction.startTransaction();
        cuentaCliente = HelperMesh.formatCuentaCliente(cuentaCliente);

        InventarioPorTopoligiaDto inventarioTopologiaHfc =
                inventarioPoller.consultarInventario(cuentaCliente, "hfc");

        DiagnosticoHfcLineaBaseResponse response = new DiagnosticoHfcLineaBaseResponse();

        if (inventarioTopologiaHfc == null || inventarioTopologiaHfc.getInventarioCPE() == null) {
            response.setStatus("Ok");
            response.setMessage(Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO);
            response.setData(List.of(
                    new DiagnosticoHfcLineaBaseDto(
                            ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO, transaction),
                            ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, transaction)
                                    .replace("{}", cuentaCliente),
                            cuentaCliente,
                            null,
                            null
                    )
            ));
            return response;
        }

        //Validar si el cable Modem esta conectado
        String formatoMacAddress;
        formatoMacAddress = HelperMesh.formatMacAddress(inventarioTopologiaHfc.getInventarioCPE().getSerialMac());

        ResponseCmDataPollerDto dataCableModem = helperLineaBaseHfc.responseCmDataPollerDto(formatoMacAddress);

        //Validar estado del CM
        if ("false".equalsIgnoreCase(dataCableModem.getAlive())) {
            response.setStatus("Ok");
            response.setMessage(ParametersConfig.getPropertyValue(Constantes.HFC_NO_ONLINE_DESCRIPCION, transaction));
            response.setData(List.of(new DiagnosticoHfcLineaBaseDto(
                    ParametersConfig.getPropertyValue(Constantes.HFC_NO_ONLINE_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.HFC_NO_ONLINE_DESCRIPCION, transaction),
                    cuentaCliente,
                    null,
                    null)));

            return response;
        }

        //Recuperar listado de vecinos
        List<NeighborStatusDto> vecinos = helperLineaBaseHfc.obtenerListadoVecinos(dataCableModem);

        //Recuperar provisioningdata
        List<ProvisioningDataDto> lstProvisioningData = helperLineaBaseHfc.aprovisionamientoCliente(dataCableModem);

        //Recuperar data del cable modem
        List<CableModemDataDto> lsCableModemDatDto = helperLineaBaseHfc.datosCableModemPorModelo(dataCableModem);


        return response;
    }

}