package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.DiagnosticoTopologiaHfcStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.TopologiaHfcConMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.TopologiaHfcSinMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.utils.HelperLineaBaseHfc;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.utils.HelperMesh;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoDto;
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
                            ParametersConfig.getPropertyValue(Constantes.HFC_INVENTARIO_NO_ENCONTRADO_CODIGO, transaction),
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
            		ParametersConfig.getPropertyValue(Constantes.HFC_NO_ONLINE_CODIGO,transaction),
            		ParametersConfig.getPropertyValue(Constantes.HFC_NO_ONLINE_DESCRIPCION, transaction));
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

            return helperLineaBaseHfc.respuestaGenerica(
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_INVENTARIO_NO_ENCONTRADO_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_INVENTARIO_NO_ENCONTRADO_DESCRIPCION, transaction),
                    cuentaCliente,
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_INVENTARIO_NO_ENCONTRADO_DESCRIPCION, transaction),
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_INVENTARIO_NO_ENCONTRADO_DESCRIPCION, transaction));
        }

        //Validar si el cable Modem esta conectado
        String formatoMacAddress;
        formatoMacAddress = HelperMesh.formatMacAddress(inventarioTopologiaHfc.getInventarioCPE().getSerialMac());

        ResponseCmDataPollerDto dataCableModem = helperLineaBaseHfc.responseCmDataPollerDto(formatoMacAddress);
        if (dataCableModem == null) {
            return helperLineaBaseHfc.respuestaGenerica(
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ACS_NO_REPORTA_DATA_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ACS_NO_REPORTA_DATA_DESCRIPCION, transaction),
                    cuentaCliente,
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ACS_NO_REPORTA_DATA_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ACS_NO_REPORTA_DATA_DESCRIPCION, transaction));
        }

        //Recuperar listado de vecinos
        List<NeighborStatusDto> vecinos = helperLineaBaseHfc.obtenerListadoVecinos(dataCableModem);
        if (vecinos == null || vecinos.isEmpty()) {
            return helperLineaBaseHfc.respuestaGenerica(
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ACS_NO_REPORTA_VECINOS_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ACS_NO_REPORTA_VECINOS_DESCRIPCION, transaction),
                    cuentaCliente,
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ACS_NO_REPORTA_VECINOS_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ACS_NO_REPORTA_VECINOS_DESCRIPCION, transaction));
        }

        //Lógica CM OFFLINE alive = false
        if ("false".equalsIgnoreCase(dataCableModem.getAlive())) {

            //Evaluar masividad de vecinos
            double porcentajeCaida = helperLineaBaseHfc.validacionRangoDeCaida(vecinos, dataCableModem);
            boolean esMasivo = porcentajeCaida >= 80.0;

            String codigo = ParametersConfig.getPropertyValue(
                    esMasivo ? Constantes.HFC_LINEA_BASE_MASIVO_CODIGO : Constantes.HFC_LINEA_BASE_NO_MASIVO_CODIGO,
                    transaction);

            String descripcion = ParametersConfig.getPropertyValue(
                    esMasivo ? Constantes.HFC_LINEA_BASE_MASIVO_DESCRIPCION : Constantes.HFC_LINEA_BASE_NO_MASIVO_DESCRIPCION,
                    transaction);

            return helperLineaBaseHfc.respuestaGenerica(codigo, descripcion, cuentaCliente,
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_OFFLINE, transaction),
                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ONLINE, transaction));
        }

        //Lógica CM ONLINE alive = true
        if ("true".equalsIgnoreCase(dataCableModem.getAlive())) {

            //Validacion upTime del CM
            boolean upTimeMayor = helperLineaBaseHfc.validacionUpTime(dataCableModem.getUptime());

            if (upTimeMayor) {
                String codigo = ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_UPTIME_MAYOR_CODIGO, transaction);
                String descripcion = ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_UPTIME_MAYOR_DESCRIPCION, transaction);

                return helperLineaBaseHfc.respuestaGenerica(codigo, descripcion, cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ONLINE, transaction),
                        ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_NO_VALIDA_VECINOS, transaction));
            } else {

                //Recuperar provisioningdata
                List<ProvisioningDataDto> lstProvisioningData = helperLineaBaseHfc.aprovisionamientoCliente(dataCableModem);
                if (lstProvisioningData == null || lstProvisioningData.isEmpty()) {
                    return helperLineaBaseHfc.respuestaGenerica(
                            ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ACS_NO_REPORTA_APROVISIONAMIENTO_CODIGO, transaction),
                            ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ACS_NO_REPORTA_APROVISIONAMIENTO_DESCRIPCION, transaction),
                            cuentaCliente,
                            ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ONLINE, transaction),
                            ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_NO_VALIDA_VECINOS, transaction));
                }


                //Recuperar data del cable modem
                List<CableModemDataDto> lstCableModemDatDto = helperLineaBaseHfc.datosCableModemPorModelo(dataCableModem);
                if (lstCableModemDatDto == null || lstCableModemDatDto.isEmpty()) {
                    return helperLineaBaseHfc.respuestaGenerica(
                            ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_POLLER_NO_REPORTA_DATA_CODIGO, transaction),
                            ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_POLLER_NO_REPORTA_DATA_DESCRIPCION, transaction),
                            cuentaCliente,
                            ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ONLINE, transaction),
                            ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_NO_VALIDA_VECINOS, transaction));
                }

                //Validar desposicionamiento del CM
                boolean desposicionamiento = helperLineaBaseHfc.validacionDesposicionamiento(lstProvisioningData, lstCableModemDatDto);
                if (desposicionamiento) {
                    String codigo = ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_DESPOSICIONADO_CODIGO, transaction);
                    String descripcion = ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_DESPOSICIONADO_DESCRIPCION, transaction);
                    return helperLineaBaseHfc.respuestaGenerica(
                            codigo,
                            descripcion,
                            cuentaCliente,
                            ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ONLINE, transaction),
                            ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_NO_VALIDA_VECINOS, transaction));

                } else {

                    //Recuperar RealtimeMeasurements
                    List<RealTimeMeasurementDto> lstRealTimeMeasurementDto = helperLineaBaseHfc.realTimeMeasurementDto(dataCableModem);
                    if (lstRealTimeMeasurementDto == null || lstRealTimeMeasurementDto.isEmpty()) {
                        return helperLineaBaseHfc.respuestaGenerica(
                                ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ACS_NO_REPORTA_MEASUREMENT_CODIGO, transaction),
                                ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ACS_NO_REPORTA_MEASUREMENT_DESCRIPCION, transaction),
                                cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ONLINE, transaction),
                                ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_NO_VALIDA_VECINOS, transaction));
                    }


                    //Validar niveles de comunicacion del CM
                    boolean nivelesCM = helperLineaBaseHfc.validacionNivelesCM(lstRealTimeMeasurementDto);
                    if (!nivelesCM) {
                        String codigo = ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_CORRECTA_CODIGO, transaction);
                        String descripcion = ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_CORRECTA_DESCRIPCION, transaction);

                        return helperLineaBaseHfc.respuestaGenerica(
                                codigo,
                                descripcion,
                                cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ONLINE, transaction),
                                ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_NO_VALIDA_VECINOS, transaction));

                    } else {

                        //Validar niveles vecinos
                        boolean nivelesVecinoCM = helperLineaBaseHfc.validacionNivelesVecinos(vecinos);

                        if (!nivelesVecinoCM) {
                            String codigo = ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_CASO_PUNTUAL_CODIGO, transaction);
                            String descripcion = ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_CASO_PUNTUAL_DESCRICION, transaction);

                            return helperLineaBaseHfc.respuestaGenerica(
                                    codigo,
                                    descripcion,
                                    cuentaCliente,
                                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ONLINE, transaction),
                                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_NO_VALIDA_VECINOS, transaction));

                        } else {

                            String codigo = ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ESCALAMIENTO_NIVEL_DOS_CODIGO, transaction);
                            String descripcion = ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ESCALAMIENTO_NIVEL_DOS_DESCRIPCION, transaction);

                            return helperLineaBaseHfc.respuestaGenerica(
                                    codigo,
                                    descripcion,
                                    cuentaCliente,
                                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_ONLINE, transaction),
                                    ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_NO_VALIDA_VECINOS, transaction));
                        }
                    }
                }
            }
        }

        String codigo = ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_CUENTA_NO_HFC_CODIGO, transaction);
        String descripcion = ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_CUENTA_NO_HFC_DESCRIPCION, transaction);

        return helperLineaBaseHfc.respuestaGenerica(
                codigo,
                descripcion,
                cuentaCliente,
                ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_NO_VALIDA_VECINOS, transaction),
                ParametersConfig.getPropertyValue(Constantes.HFC_LINEA_BASE_CM_NO_VALIDA_VECINOS, transaction));
    }

}