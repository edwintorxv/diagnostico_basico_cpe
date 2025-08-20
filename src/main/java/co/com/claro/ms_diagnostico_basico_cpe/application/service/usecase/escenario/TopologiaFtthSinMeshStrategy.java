package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.ConsultarParametrosDipositivosService;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.validator.CanalWifiValidator;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceStatusDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceStatusResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopologiaFtthSinMeshStrategy implements DiagnosticoTopologiaFtthStrategy {

    private final ConsultarParametrosDipositivosService consultarParametrosDipositivosService;
    private final CanalWifiValidator canalWifiValidator;

    public TopologiaFtthSinMeshStrategy(CanalWifiValidator canalWifiValidator,
                                        ConsultarParametrosDipositivosService consultarParametrosDipositivosService) {
        this.consultarParametrosDipositivosService = consultarParametrosDipositivosService;
        this.canalWifiValidator = canalWifiValidator;
    }

    @Override
    public DiagnosticoFtthResponse diagnosticar(InventarioPorTopoligiaDto inventario, IAcsPortOut acsPortOut) {
        String cuentaCliente = inventario.getCuentaCliente();
        try {

            // CPE principal viene del DTO directamente
            InventarioPorClienteDto cpePrincipal = inventario.getInventarioCPE();

            if (cpePrincipal == null) {
                return diagnostico(cuentaCliente,
                        "CPE_NO_ENCONTRADO",
                        "No se encontró equipo principal FTTH en inventario");
            }

            DeviceStatusResponse deviceStatus = acsPortOut.obtenerEstadoPorSerial(cpePrincipal.getSerialNumber());
            if (deviceStatus == null || deviceStatus.getData().isEmpty()) {
                return diagnostico(cuentaCliente,
                        "SIN_RESPUESTA_ACS",
                        "No se pudo obtener estado del CPE en ACS");
            }


            DeviceStatusDto statusDto = deviceStatus.getData().get(0);

            if ("false".equalsIgnoreCase(statusDto.getOnline())) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_NO_ONLINE_CODIGO,
                        Constantes.ONT_NO_ONLINE_DESCRIPCION);
            }

            if (deviceStatus == null) {

                return new DiagnosticoFtthResponse(
                        "OK",
                        ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                        List.of(new DiagnosticoFtthDto(
                                cuentaCliente,
                                "SIN_RESPUESTA_ACS",
                                "No se pudo obtener estado del CPE en ACS"
                        ))
                );
            }

            DeviceParamsDto deviceParamsDto = new DeviceParamsDto();

            deviceParamsDto.setDevicesn(cpePrincipal.getSerialNumber());
            deviceParamsDto.setSource(("1"));
            deviceParamsDto.setOUI("");
            deviceParamsDto.setModelname("");
            deviceParamsDto.setNames(true);
            deviceParamsDto.setValues(false);
            deviceParamsDto.setAttributes(true);
            deviceParamsDto.setCreator("");
            deviceParamsDto.setAppid("");
            deviceParamsDto.setKeyOrTree(String.format(Constantes.KEY_OR_TREE_ONT, cpePrincipal.getMarca().toLowerCase(),
                    cpePrincipal.getModelo().toLowerCase()));

            DeviceParamsResponse response = consultarParametrosDipositivosService.consultarParametrosDispositivo(deviceParamsDto);


            boolean wifiOk = canalWifiValidator.validar(response, deviceParamsDto.getKeyOrTree());


            if (wifiOk) {

                return new DiagnosticoFtthResponse(
                        "OK",
                        ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                        List.of(new DiagnosticoFtthDto(
                                cuentaCliente,
                                Constantes.ONT_ONLINE_SIN_ULTRAWIFI_CANALES_ONLINE_CODIGO,
                                Constantes.ONT_ONLINE_SIN_ULTRAWIFI_CANALES_ONLINE_DESCRIPCION
                        ))
                );
            } else {

                return new DiagnosticoFtthResponse(
                        "OK",
                        ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                        List.of(new DiagnosticoFtthDto(
                                cuentaCliente,
                                Constantes.ONT_ONLINE_SIN_ULTRAWIFI_CANALES_OFFLINE_CODIGO,
                                Constantes.ONT_ONLINE_SIN_ULTRAWIFI_CANALES_OFFLINE_DESCRIPCION
                        ))
                );
            }

        } catch (Exception e) {
            return new DiagnosticoFtthResponse(
                    "OK",
                    ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                    List.of(new DiagnosticoFtthDto(cuentaCliente,
                            "ERROR_INTERNO",
                            "Error ejecutando diagnóstico: " + e.getMessage()
                    ))
            );
        }
    }

    private DiagnosticoFtthResponse diagnostico(String cuenta, String codigo, String descripcion) {
        return new DiagnosticoFtthResponse("OK",
                ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                List.of(new DiagnosticoFtthDto(
                        cuenta,
                        codigo,
                        descripcion
                ))
        );
    }
}
