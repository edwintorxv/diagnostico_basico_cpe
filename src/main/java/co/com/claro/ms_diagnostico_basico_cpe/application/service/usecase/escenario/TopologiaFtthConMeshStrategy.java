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
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public DiagnosticoFtthResponse diagnosticar(InventarioPorTopoligiaDto topologia,
                                                IAcsPortOut acsPortOut) {

        final String cuentaCliente = topologia.getCuentaCliente();

        try {

            InventarioPorClienteDto cpePrincipal = topologia.getInventarioCPE();
            List<InventarioPorClienteDto> meshList = Optional.ofNullable(topologia.getLstinventarioMesh())
                    .orElseGet(List::of);

            if (cpePrincipal == null) {
                return errorInventario(cuentaCliente);
            }

            List<InventarioPorClienteDto> equipos = new ArrayList<>();
            equipos.add(cpePrincipal);
            equipos.addAll(meshList);


            List<String> seriales = equipos.stream()
                    .map(this::serialInventarioNormalizado) // toma serialNumber o serialMac
                    .filter(Objects::nonNull)
                    .toList();


            DeviceParamsDto dtoOnt = buildDeviceParamsDto(
                    cpePrincipal.getSerialNumber(),
                    String.format(Constantes.KEY_OR_TREE_ONT,
                            safeLower(cpePrincipal.getMarca()),
                            safeLower(cpePrincipal.getModelo()).replace(" ", "-")),
                    cpePrincipal.getMarca(),
                    cpePrincipal.getModelo()
            );

            DeviceStatusResponse deviceStatus = acsPortOut.obtenerEstadoPorSerial(cpePrincipal.getSerialNumber());
            if (deviceStatus == null || deviceStatus.getData() == null || deviceStatus.getData().isEmpty()) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_NO_ONLINE_CODIGO,
                        Constantes.ONT_NO_ONLINE_DESCRIPCION);
            }

            DeviceStatusDto statusDto = deviceStatus.getData().get(0);
            if (statusDto == null || "false".equalsIgnoreCase(statusDto.getOnline())) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_NO_ONLINE_CODIGO,
                        Constantes.ONT_NO_ONLINE_DESCRIPCION);
            }

            // 2) Parámetros ONT
            DeviceParamsResponse responseOnt = consultarParametrosDipositivosService.consultarParametrosDispositivo(dtoOnt);
            if (isAcsDataEmpty(responseOnt)) {
                return diagnostico(cuentaCliente,
                        Constantes.ACS_NO_REPORTA_DATA_CODIGO,
                        Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION);
            }

            List<String> macsOnt = normalizarListaMac(
                    macAdreesValidator.macAddressDetectadas(responseOnt, dtoOnt.getKeyOrTree())
            );

            boolean wifiOntOk = canalWifiValidator.validar(responseOnt, dtoOnt.getKeyOrTree());

            // 3) Validar cantidad de MAC conectadas a ONT
            long conteoCoincidenciasOnt = macsOnt.stream().filter(seriales::contains).count();
            if (conteoCoincidenciasOnt > 1) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_CODIGO,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_DESCRIPCION);
            }

            // 4) Buscar AP maestro en inventario
            InventarioPorClienteDto meshMaster = findInventarioCoincidente(equipos, macsOnt);
            if (meshMaster == null) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTA_AP_MAESTRO_CODIGO,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTA_AP_MAESTRO_DESCRIPCION);
            }

            // Validar estado AP maestro
            if (!estaOnline(serialPreferido(meshMaster), acsPortOut)) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_CODIGO,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_DESCRIPCION);
            }

            // 5) Consultar parámetros AP maestro
            DeviceParamsDto dtoMesh = buildDeviceParamsDto(
                    serialPreferido(meshMaster),
                    Constantes.KEY_OR_TREE_MESH,
                    safeLower(meshMaster.getMarca()),
                    meshMaster.getModelo()
            );

            DeviceParamsResponse responseMesh = consultarParametrosDipositivosService.consultarParametrosDispositivo(dtoMesh);
            if (isAcsDataEmpty(responseMesh)) {
                return diagnostico(cuentaCliente,
                        Constantes.ACS_NO_REPORTA_DATA_CODIGO,
                        Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION);
            }

            List<String> macsMesh = normalizarListaMac(
                    macAdreesValidator.macAddressDetectadas(responseMesh, dtoMesh.getKeyOrTree())
            );

            if (macsMesh.stream().filter(seriales::contains).count() == 0) {
                return diagnostico(cuentaCliente,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_SIN_AP_ESCLAVO_CODIGO,
                        Constantes.ONT_ONLINE_CON_ULTRAWIFI_SIN_AP_ESCLAVO_DESCRIPCION);
            }

            // 6) Buscar AP esclavo
            InventarioPorClienteDto meshSlave = findInventarioCoincidente(equipos, macsMesh);
            if (meshSlave == null) {
                return errorInventario(cuentaCliente);
            }

            // Consultar AP esclavo
            DeviceParamsDto dtoSlave = buildDeviceParamsDto(
                    serialPreferido(meshSlave),
                    Constantes.KEY_OR_TREE_MESH,
                    meshMaster.getMarca(),
                    meshMaster.getModelo()
            );

            DeviceParamsResponse responseSlave = consultarParametrosDipositivosService.consultarParametrosDispositivo(dtoSlave);
            if (isAcsDataEmpty(responseSlave)) {
                return diagnostico(cuentaCliente,
                        Constantes.ACS_NO_REPORTA_DATA_CODIGO,
                        Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION);
            }

            boolean wifiSlaveOk = canalWifiValidator.validar(responseSlave, dtoSlave.getKeyOrTree());

            // 7) Diagnósticos finales de canales
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

    private String norm(String s) {
        return (s == null) ? null : s.replace(":", "").toUpperCase();
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    private String serialInventarioNormalizado(InventarioPorClienteDto i) {
        // Usa serialNumber si existe; si no, serialMac
        String raw = (i.getSerialNumber() != null && !i.getSerialNumber().isEmpty())
                ? i.getSerialNumber()
                : i.getSerialMac();
        return norm(raw);
    }

    private List<String> normalizarListaMac(List<String> macs) {
        if (macs == null) return List.of();
        return macs.stream().map(this::norm).filter(Objects::nonNull).toList();
    }

    private String serialPreferido(InventarioPorClienteDto i) {
        return (i.getSerialMac() != null && !i.getSerialMac().isEmpty())
                ? i.getSerialMac()
                : i.getSerialNumber();
    }

}
