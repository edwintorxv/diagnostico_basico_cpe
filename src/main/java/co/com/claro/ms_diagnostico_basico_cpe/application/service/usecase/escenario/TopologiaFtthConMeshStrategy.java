package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.ConsultarParametrosDipositivosService;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.validator.CanalWifiValidator;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.validator.MacAdreesValidator;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceStatusDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceStatusResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopologiaFtthConMeshStrategy implements DiagnosticoTopologiaFtthStrategy {

    private final ConsultarParametrosDipositivosService consultarParametrosDipositivosService;
    private final MacAdreesValidator macAdreesValidator;
    private final CanalWifiValidator canalWifiValidator;

    public TopologiaFtthConMeshStrategy(CanalWifiValidator canalWifiValidator,
                                        ConsultarParametrosDipositivosService consultarParametrosDipositivosService,
                                        MacAdreesValidator macAdreesValidator) {
        this.canalWifiValidator = canalWifiValidator;
        this.consultarParametrosDipositivosService = consultarParametrosDipositivosService;
        this.macAdreesValidator = macAdreesValidator;
    }

    @Override
    public DiagnosticoFtthResponse diagnosticar(String cuentaCliente,
                                                List<InventarioPorClienteDto> inventario,
                                                IAcsPortOut acsPortOut) {

        try {
            List<String> seriales = inventario.stream()
                    .map(InventarioPorClienteDto::getSerialNumber)
                    .toList();

            // 1. Buscar ONT principal
            InventarioPorClienteDto cpePrincipal = inventario.stream()
                    .filter(i -> "ftth".equalsIgnoreCase(i.getProducto()))
                    .findFirst()
                    .orElse(null);

            if (cpePrincipal == null) {
                return errorInventario(cuentaCliente);
            }

            DeviceParamsDto dtoOnt = buildDeviceParamsDto(
                    cpePrincipal.getSerialNumber(),
                    String.format(Constantes.KEY_OR_TREE_ONT, cpePrincipal.getMarca().toLowerCase(),
                            cpePrincipal.getModelo().replace(" ", "-").toLowerCase()),
                    cpePrincipal.getMarca(),
                    cpePrincipal.getModelo()
            );

            DeviceStatusResponse deviceStatus = acsPortOut.obtenerEstadoPorSerial(cpePrincipal.getSerialNumber());

            DeviceStatusDto statusDto = deviceStatus.getData().get(0);

            if ("false".equalsIgnoreCase(statusDto.getOnline())) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_NO_ONLINE_CODIGO,
                        Constantes.ONT_NO_ONLINE_DESCRIPCION);
            }

            DeviceParamsResponse responseOnt = consultarParametrosDipositivosService.consultarParametrosDispositivo(dtoOnt);
            if (isAcsDataEmpty(responseOnt)) {
                return diagnostico(cuentaCliente,
                        Constantes.ACS_NO_REPORTA_DATA_CODIGO,
                        Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION
                );
            }

            List<String> macsOnt = macAdreesValidator.macAddressDetectadas(responseOnt, dtoOnt.getKeyOrTree());
            boolean wifiOntOk = canalWifiValidator.validar(responseOnt, dtoOnt.getKeyOrTree());

            // 3. Validar cantidad de MAC conectadas a ONT
            if (macsOnt.stream().filter(seriales::contains).count() > 1) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_CODIGO,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_DESCRIPCION);
            }

            // 4. Buscar AP maestro en inventario
            InventarioPorClienteDto meshMaster = findInventarioCoincidente(inventario, macsOnt);
            if (meshMaster == null) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTA_AP_MAESTRO_CODIGO,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTA_AP_MAESTRO_DESCRIPCION);
            }

            // Validar estado AP maestro
            if (!estaOnline(meshMaster.getSerialMac(), acsPortOut)) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_CODIGO,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_DESCRIPCION);
            }

            // 5. Consultar parámetros AP maestro
            DeviceParamsDto dtoMesh = buildDeviceParamsDto(
                    meshMaster.getSerialMac(),
                    Constantes.KEY_OR_TREE_MESH,
                    meshMaster.getMarca().toLowerCase(),
                    meshMaster.getModelo().toLowerCase()
            );

            DeviceParamsResponse responseMesh = consultarParametrosDipositivosService.consultarParametrosDispositivo(dtoMesh);
            if (isAcsDataEmpty(responseMesh)) {
                return diagnostico(cuentaCliente,
                        Constantes.ACS_NO_REPORTA_DATA_CODIGO,
                        Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION
                );
            }

            List<String> macsMesh = macAdreesValidator.macAddressDetectadas(responseMesh, dtoMesh.getKeyOrTree());

            if (macsMesh.stream().filter(seriales::contains).count() == 0) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_SIN_AP_ESCLAVO_CODIGO,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_SIN_AP_ESCLAVO_DESCRIPCION);
            }

            // 6. Buscar AP esclavo
            InventarioPorClienteDto meshSlave = findInventarioCoincidente(inventario, macsMesh);
            if (meshSlave == null) return errorInventario(cuentaCliente);

            // Consultar AP esclavo
            DeviceParamsDto dtoSlave = buildDeviceParamsDto(
                    meshSlave.getSerialMac(),
                    Constantes.KEY_OR_TREE_MESH,
                    meshMaster.getMarca(),
                    meshMaster.getModelo()
            );
            DeviceParamsResponse responseSlave = consultarParametrosDipositivosService.consultarParametrosDispositivo(dtoSlave);
            if (isAcsDataEmpty(responseSlave)) {
                return diagnostico(cuentaCliente,
                        Constantes.ACS_NO_REPORTA_DATA_CODIGO,
                        Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION
                );
            }

            boolean wifiSlaveOk = canalWifiValidator.validar(responseSlave, dtoSlave.getKeyOrTree());

            // 7. Diagnósticos finales de canales
            if (!wifiSlaveOk) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_CODIGO,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_DESCRIPCION);
            }
            if (wifiOntOk && wifiSlaveOk) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_CANALES_ONLINE_AP_ONT_CODIGO,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_CANALES_ONLINE_AP_ONT_DESCRIPCION);
            }
            if (!wifiOntOk && wifiSlaveOk) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_ONT_ONLINE_AP_CODIGO,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_ONT_ONLINE_AP_DESCRIPCION);
            }
            return errorInventario(cuentaCliente);

        } catch (Exception e) {
            return new DiagnosticoFtthResponse(
                    "OK",
                    ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                    List.of(new DiagnosticoFtthDto(
                            cuentaCliente,
                            Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO,
                            String.format(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, cuentaCliente)
                    ))
            );

        }
    }

    private DiagnosticoFtthResponse errorInventario(String cuentaCliente) {
        return new DiagnosticoFtthResponse(
                "OK",
                ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                List.of(new DiagnosticoFtthDto(
                        cuentaCliente,
                        Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO,
                        String.format(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, cuentaCliente)
                ))
        );
    }

    private DeviceParamsDto buildDeviceParamsDto(String serial, String keyTemplate, String marca, String modelo) {
        DeviceParamsDto dto = new DeviceParamsDto();
        dto.setDevicesn(serial);
        dto.setSource("1");
        dto.setOUI("");
        dto.setModelname("");
        dto.setNames(true);
        dto.setValues(false);
        dto.setAttributes(true);
        dto.setCreator("");
        dto.setAppid("");
        dto.setKeyOrTree(String.format(keyTemplate, marca, modelo.replace(" ", "-").toLowerCase()));
        return dto;
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

    private InventarioPorClienteDto findInventarioCoincidente(List<InventarioPorClienteDto> inventario, List<String> macs) {
        return inventario.stream()
                .filter(i -> i.getSerialNumber() != null)
                .filter(i -> macs.contains(i.getSerialNumber().replace(":", "").toUpperCase()))
                .findFirst()
                .orElse(null);
    }

    private boolean estaOnline(String serial, IAcsPortOut acsPortOut) throws Exception {
        DeviceStatusResponse status = acsPortOut.obtenerEstadoPorSerial(serial);
        return status.getData() != null &&
                status.getData().stream().anyMatch(d -> "true".equalsIgnoreCase(d.getOnline()));
    }

    //Valida que ACS no retorne null o vacios
    private boolean isAcsDataEmpty(DeviceParamsResponse response) {
        return response == null
                || response.getData() == null
                || response.getData().isEmpty()
                || response.getData().get(0).getParams() == null
                || response.getData().get(0).getParams().isEmpty();
    }

}
