package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.DiagnosticoTopologiaFtthStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.TopologiaFtthConMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.TopologiaFtthSinMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.utils.HelperLineaBaseFtth;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.utils.HelperMesh;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.ResponseDiagnosticoFtthLineaBaseDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.NeighborStatusFtthDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.RealTimeMeasurementFtthDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseOntDataDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico.IDiagnosticoFTTHPortIn;
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
public class DiagnosticoTopologiaFtth implements IDiagnosticoFTTHPortIn {

    Transaction transaction = Transaction.startTransaction();

    private final IAcsPortOut acsPortOut;
    private final IPollerPortOut pollerPortOut;


    private final TopologiaFtthSinMeshStrategy ftthSinMeshsStrategy;
    private final TopologiaFtthConMeshStrategy ftthConMeshsStrategy;
    private final HelperLineaBaseFtth helperLineaBaseFtth;
    private final InventarioPoller inventarioPoller;

    @Override
    public DiagnosticoFtthResponse diagnosticoTopologiaFtth(String cuentaCliente) throws Exception {

        cuentaCliente = HelperMesh.formatCuentaCliente(cuentaCliente);


        InventarioPorTopoligiaDto inventarioTopologiaFtth =
                inventarioPoller.consultarInventario(cuentaCliente, "ftth");

        if (inventarioTopologiaFtth == null || inventarioTopologiaFtth.getInventarioCPE() == null) {
            return new DiagnosticoFtthResponse(
                    "OK",
                    ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                    List.of(new DiagnosticoFtthDto(
                            cuentaCliente,
                            ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO, transaction),
                            ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, transaction)
                                    .replace("{}", cuentaCliente)
                    ))
            );
        }

        DiagnosticoTopologiaFtthStrategy strategy =
                (inventarioTopologiaFtth.getLstinventarioMesh() == null || inventarioTopologiaFtth.getLstinventarioMesh().isEmpty())
                        ? ftthSinMeshsStrategy
                        : ftthConMeshsStrategy;

        return strategy.diagnosticar(inventarioTopologiaFtth, acsPortOut);
    }

    @Override
    public ResponseDiagnosticoFtthLineaBaseDto diagnosticoLineaBase(String cuentaCliente) throws Exception {

        cuentaCliente = HelperMesh.formatCuentaCliente(cuentaCliente);

        //Recuperar inventario
        InventarioPorTopoligiaDto inventrarioLineaBaseFtth =
                inventarioPoller.consultarInventario(cuentaCliente, "ftth");

        //Validar existencia de inventario
        if (inventrarioLineaBaseFtth == null) {
            return helperLineaBaseFtth.respuestaGenerica(
                    ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_NO_EXISTE_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_NO_EXISTE_DESCRIPCION, transaction),
                    cuentaCliente,
                    ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_NO_VALIDA_VECINOS, transaction),
                    ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_NO_VALIDA_VECINOS, transaction)
            );
        }

        //Formatea MacAddress Recuperada del inventario
        String formatoMacAddress;
        formatoMacAddress = HelperMesh.formatMacAddress(inventrarioLineaBaseFtth.getInventarioCPE().getSerialMac());

        //Recuperar data de Ont
        ResponseOntDataDto responseOntDataDto = helperLineaBaseFtth.ontData(formatoMacAddress);

        //Validacion respuesa
        if (responseOntDataDto == null) {
            return helperLineaBaseFtth.respuestaGenerica(
                    ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_POLLER_NO_REPORTA_DATA_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_POLLER_NO_REPORTA_DATA_DESCRIPCION, transaction),
                    cuentaCliente,
                    ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_NO_VALIDA_VECINOS, transaction),
                    ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_NO_VALIDA_VECINOS, transaction)
            );
        }

        //Recuperar vecinos
        List<NeighborStatusFtthDto> neighborStatusFtthDto = helperLineaBaseFtth.vecinos(responseOntDataDto);
        if (neighborStatusFtthDto == null) {
            return helperLineaBaseFtth.respuestaGenerica(
                    ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_POLLER_NO_REPORTA_VECINOS_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_POLLER_NO_REPORTA_VECINOS_DESCRIPCION, transaction),
                    cuentaCliente,
                    ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_NO_VALIDA_VECINOS, transaction),
                    ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_NO_VALIDA_VECINOS, transaction)
            );
        }

        //Flujo ONT Alive = false
        if ("false".equalsIgnoreCase(responseOntDataDto.getData().getAlive())) {

            //Validar masividad
            boolean caidaMasiva = helperLineaBaseFtth.masividadVecinos(neighborStatusFtthDto);
            if (caidaMasiva) {
                return helperLineaBaseFtth.respuestaGenerica(
                        ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_VECINOS_OFFLINE_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_VECINOS_OFFLINE_DESCRIPCION, transaction),
                        cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_OFFLINE, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_OFFLINE, transaction)
                );
            } else {
                return helperLineaBaseFtth.respuestaGenerica(
                        ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_DESENGANCHADA_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_DESENGANCHADA_DESCRIPCION, transaction),
                        cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_OFFLINE, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_ONLINE, transaction)
                );
            }
        }
        //Flujo ONT Alive = true
        if ("true".equalsIgnoreCase(responseOntDataDto.getData().getAlive())) {
            boolean upTimeMayor = helperLineaBaseFtth.upTime(responseOntDataDto);

            if (upTimeMayor) {
                return helperLineaBaseFtth.respuestaGenerica(
                        ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_REINICIO_AUTOMATICO_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_REINICIO_AUTOMATICO_DESCRIPCION, transaction),
                        cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_ONLINE, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_NO_VALIDA_VECINOS, transaction)
                );
            } else {
                //Recuperacion RealtimeMeasurement
                RealTimeMeasurementFtthDto realTimeMeasurementFtthDto = helperLineaBaseFtth.realtimeMeasurement(responseOntDataDto).getData();
                if (realTimeMeasurementFtthDto == null) {
                    return helperLineaBaseFtth.respuestaGenerica(
                            ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_POLLER_NO_REPORTA_REALTIMEMEASUREMENT_CODIGO, transaction),
                            ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_POLLER_NO_REPORTA_REALTIMEMEASUREMENT_DESCRIPCION, transaction),
                            cuentaCliente,
                            ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_NO_VALIDA_VECINOS, transaction),
                            ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_NO_VALIDA_VECINOS, transaction)
                    );
                }

                boolean ontFueraDeNiveles = helperLineaBaseFtth.validacioNivelesOnt(realTimeMeasurementFtthDto);
                if (!ontFueraDeNiveles) {
                    return helperLineaBaseFtth.respuestaGenerica(
                            ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_CORECTA_CODIGO, transaction),
                            ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_LINEA_BASE_CORECTA_DESCRIPCION, transaction),
                            cuentaCliente,
                            ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_ONLINE, transaction),
                            ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_NO_VALIDA_VECINOS, transaction)
                    );
                } else {
                    boolean vecinosFueraDeNiveles = helperLineaBaseFtth.validacionNivelesOntVecinos(neighborStatusFtthDto);
                    if (vecinosFueraDeNiveles) {
                        return helperLineaBaseFtth.respuestaGenerica(
                                ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_POSIBLE_INCIDENTE_MASIVO_CODIGO, transaction),
                                ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_POSIBLE_INCIDENTE_MASIVO_DESCRIPCION, transaction),
                                cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_ONLINE, transaction),
                                ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_ONLINE, transaction)
                        );
                    } else {
                        return helperLineaBaseFtth.respuestaGenerica(
                                ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_CASO_PUNTUAL_CODIGO, transaction),
                                ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_CASO_PUNTUAL_DESCRIPCION, transaction),
                                cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_ONLINE, transaction),
                                ParametersConfig.getPropertyValue(Constantes.FTTH_LINEA_BASE_ONT_ONLINE, transaction)
                        );
                    }
                }
            }
        }
        return helperLineaBaseFtth.respuestaGenerica(
                ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO, transaction),
                ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, transaction),
                cuentaCliente,
                ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO, transaction),
                ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, transaction)
        );
    }
}
