package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.ConsultarParametrosDipositivosService;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.validator.CanalWifiValidator;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceStatusDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceStatusResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.ParametersConfig;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopologiaFtthSinMeshStrategy implements DiagnosticoTopologiaFtthStrategy {

    Transaction transaction = Transaction.startTransaction();

    private final ConsultarParametrosDipositivosService consultarParametrosDipositivosService;
    private final CanalWifiValidator canalWifiValidator;

    public TopologiaFtthSinMeshStrategy(CanalWifiValidator canalWifiValidator,
                                        ConsultarParametrosDipositivosService consultarParametrosDipositivosService) {
        this.consultarParametrosDipositivosService = consultarParametrosDipositivosService;
        this.canalWifiValidator = canalWifiValidator;
    }

    @Override
    public DiagnosticoResponse diagnosticar(InventarioPorTopoligiaDto inventario, IAcsPortOut acsPortOut) {
        String cuentaCliente = inventario.getCuentaCliente();
        try {

            // CPE principal viene del DTO directamente
            InventarioPorClienteDto cpePrincipal = inventario.getInventarioCPE();

            if (cpePrincipal == null) {
                return diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_CPE_NO_ENCONTRADO_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_CPE_NO_ENCONTRADO_DESCRIPCION, transaction));
            }

            DeviceStatusResponse deviceStatus = acsPortOut.obtenerEstadoPorSerial(cpePrincipal.getSerialNumber());
            if (deviceStatus == null || deviceStatus.getData().isEmpty()) {
                return diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.ACS_NO_REPORTA_DATA_CODIGO, Transaction.startTransaction()),
                        ParametersConfig.getPropertyValue(Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION, Transaction.startTransaction()));
            }


            DeviceStatusDto statusDto = deviceStatus.getData().get(0);

            if ("false".equalsIgnoreCase(statusDto.getOnline())) {
                return diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_NO_ONLINE_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_NO_ONLINE_DESCRIPCION, transaction));
            }

            if (deviceStatus == null) {

                return new DiagnosticoResponse(
                        "OK",
                        ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                        List.of(new DiagnosticoDto(
                                cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.ACS_NO_REPORTA_DATA_CODIGO, Transaction.startTransaction()),
                                ParametersConfig.getPropertyValue(Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION, Transaction.startTransaction())
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

                return new DiagnosticoResponse(
                        "OK",
                        ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                        List.of(new DiagnosticoDto(
                                cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_SIN_ULTRAWIFI_CANALES_ONLINE_CODIGO, transaction),
                                ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_SIN_ULTRAWIFI_CANALES_ONLINE_DESCRIPCION, transaction)
                        ))
                );
            } else {

                return new DiagnosticoResponse(
                        "OK",
                        ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                        List.of(new DiagnosticoDto(
                                cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_SIN_ULTRAWIFI_CANALES_OFFLINE_CODIGO, transaction),
                                ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_SIN_ULTRAWIFI_CANALES_OFFLINE_DESCRIPCION, transaction)
                        ))
                );
            }

        } catch (Exception e) {
            return new DiagnosticoResponse(
                    "OK",
                    ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                    List.of(new DiagnosticoDto(cuentaCliente,
                            "600",
                            "Error ejecutando diagnóstico: " + e.getMessage()
                    ))
            );
        }
    }

    private DiagnosticoResponse diagnostico(String cuenta, String codigo, String descripcion) {
        return new DiagnosticoResponse("OK",
                ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                List.of(new DiagnosticoDto(
                        cuenta,
                        codigo,
                        descripcion
                ))
        );
    }
}
