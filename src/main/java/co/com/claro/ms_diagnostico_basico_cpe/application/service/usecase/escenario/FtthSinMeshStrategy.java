package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.ConsultarParametrosDipositivosService;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.validator.CanalWifiValidator;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceStatusResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFTTHResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FtthSinMeshStrategy implements DiagnosticoStrategy {

    private final ConsultarParametrosDipositivosService consultarParametrosDipositivosService;
    private final CanalWifiValidator canalWifiValidator;

    public FtthSinMeshStrategy(CanalWifiValidator canalWifiValidator,
                               ConsultarParametrosDipositivosService consultarParametrosDipositivosService, CanalWifiValidator canalWifiValidator1) {
        this.consultarParametrosDipositivosService = consultarParametrosDipositivosService;
        this.canalWifiValidator = canalWifiValidator;
    }

    @Override
    public DiagnosticoFTTHResponse diagnosticar(String cuentaCliente,
                                                List<InventarioPorClienteDto> inventario,
                                                IAcsPortOut acsPortOut) {


        try {

            InventarioPorClienteDto cpePrincipal = inventario.stream()
                    .filter(i -> "ftth".equalsIgnoreCase(i.getProducto()))
                    .findFirst()
                    .orElse(null);

            if (cpePrincipal == null) {
                return new DiagnosticoFTTHResponse(
                        cuentaCliente,
                        "CPE_NO_ENCONTRADO",
                        "No se encontró equipo principal FTTH en inventario"
                );
            }

            DeviceStatusResponse deviceStatus = acsPortOut.obtenerEstadoPorSerial(cpePrincipal.getSerialNumber());

            if (deviceStatus == null) {
                return new DiagnosticoFTTHResponse(
                        cuentaCliente,
                        "SIN_RESPUESTA_ACS",
                        "No se pudo obtener estado del CPE en ACS"
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
            deviceParamsDto.setKeyOrTree(String.format(Constantes.KEY_OR_TREE_ONT, cpePrincipal.getMarca(), cpePrincipal.getModelo()));

            DeviceParamsResponse response = consultarParametrosDipositivosService.consultarParametrosDispositivo(deviceParamsDto);


            boolean wifiOk = canalWifiValidator.validar(response, deviceParamsDto.getKeyOrTree());


            if (wifiOk) {
                return new DiagnosticoFTTHResponse(
                        cuentaCliente,
                        Constantes.ONT_ONLINE_SIN_ULTRAWIFI_CANALES_ONLINE_CODIGO,
                        Constantes.ONT_ONLINE_SIN_ULTRAWIFI_CANALES_ONLINE_DESCRIPCION
                );
            } else {
                return new DiagnosticoFTTHResponse(
                        cuentaCliente,
                        Constantes.ONT_ONLINE_SIN_ULTRAWIFI_CANALES_OFFLINE_CODIGO,
                        Constantes.ONT_ONLINE_SIN_ULTRAWIFI_CANALES_OFFLINE_DESCRIPCION
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new DiagnosticoFTTHResponse(
                    cuentaCliente,
                    "ERROR_INTERNO",
                    "Error ejecutando diagnóstico: " + e.getMessage()
            );
        }
    }
}
