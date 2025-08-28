package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.ConsultarParametrosDipositivosService;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.utils.HelperMesh;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.validator.CanalWifiValidator;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.validator.MacAdreesValidator;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TopologiaFtthConMeshStrategy implements DiagnosticoTopologiaFtthStrategy {

    Transaction transaction = Transaction.startTransaction();

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
    public DiagnosticoResponse diagnosticar(InventarioPorTopoligiaDto topologia,
                                            IAcsPortOut acsPortOut) {


        final String cuentaCliente = topologia.getCuentaCliente();

        Transaction transaction = Transaction.startTransaction();
        try {

            InventarioPorClienteDto cpePrincipal = topologia.getInventarioCPE();
            List<InventarioPorClienteDto> meshList = Optional.ofNullable(topologia.getLstinventarioMesh())
                    .orElseGet(List::of);

            if (cpePrincipal == null) {
                return HelperMesh.errorInventario(cuentaCliente, transaction);
            }

            List<InventarioPorClienteDto> equipos = new ArrayList<>();
            equipos.add(cpePrincipal);
            equipos.addAll(meshList);


            List<String> seriales = equipos.stream()
                    .map(HelperMesh::serialInventarioNormalizado) // toma serialNumber o serialMac
                    .filter(Objects::nonNull)
                    .toList();


            DeviceParamsDto dtoOnt = HelperMesh.buildDeviceParamsDto(
                    cpePrincipal.getSerialNumber(),
                    String.format(Constantes.KEY_OR_TREE_ONT,
                            HelperMesh.safeLower(cpePrincipal.getMarca()),
                            HelperMesh.safeLower(cpePrincipal.getModelo()).replace(" ", "-")),
                    cpePrincipal.getMarca(),
                    cpePrincipal.getModelo()
            );

            DeviceStatusResponse deviceStatus = acsPortOut.obtenerEstadoPorSerial(cpePrincipal.getSerialNumber());
            if (deviceStatus == null || deviceStatus.getData() == null || deviceStatus.getData().isEmpty()) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_NO_ONLINE_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_NO_ONLINE_DESCRIPCION, transaction));
            }

            DeviceStatusDto statusDto = deviceStatus.getData().get(0);
            if (statusDto == null || "false".equalsIgnoreCase(statusDto.getOnline())) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_NO_ONLINE_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_NO_ONLINE_DESCRIPCION, transaction));
            }

            // 2) Parámetros ONT
            DeviceParamsResponse responseOnt = consultarParametrosDipositivosService.consultarParametrosDispositivo(dtoOnt);
            if (HelperMesh.isAcsDataEmpty(responseOnt)) {
                return HelperMesh.diagnostico(cuentaCliente,
                        Constantes.ACS_NO_REPORTA_DATA_CODIGO,
                        Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION);
            }

            List<String> macsOnt = HelperMesh.normalizarListaMac(
                    macAdreesValidator.macAddressDetectadas(responseOnt, dtoOnt.getKeyOrTree())
            );

            boolean wifiOntOk = canalWifiValidator.validar(responseOnt, dtoOnt.getKeyOrTree());

            // 3) Validar cantidad de MAC conectadas a ONT
            long conteoCoincidenciasOnt = macsOnt.stream().filter(seriales::contains).count();
            if (conteoCoincidenciasOnt > 1) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_DESCRIPCION, transaction));
            }

            // 4) Buscar AP maestro en inventario
            InventarioPorClienteDto meshMaster = HelperMesh.findInventarioCoincidente(equipos, macsOnt);
            if (meshMaster == null) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTA_AP_MAESTRO_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTA_AP_MAESTRO_DESCRIPCION, transaction));
            }


            // Validar estado AP maestro
            if (!HelperMesh.estaOnline(HelperMesh.serialPreferido(meshMaster), acsPortOut)) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_DESCRIPCION, transaction));
            }

            // 5) Consultar parámetros AP maestro
            DeviceParamsDto dtoMesh = HelperMesh.buildDeviceParamsDto(
                    HelperMesh.serialPreferido(meshMaster),
                    Constantes.KEY_OR_TREE_MESH,
                    HelperMesh.safeLower(meshMaster.getMarca()),
                    meshMaster.getModelo()
            );


            DeviceParamsResponse responseMesh = consultarParametrosDipositivosService.consultarParametrosDispositivo(dtoMesh);

            if (HelperMesh.isAcsDataEmpty(responseMesh)) {
                return HelperMesh.diagnostico(cuentaCliente,
                        Constantes.ACS_NO_REPORTA_DATA_CODIGO,
                        Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION);
            }

            List<String> macsMesh = HelperMesh.normalizarListaMac(
                    macAdreesValidator.macAddressDetectadas(responseMesh, dtoMesh.getKeyOrTree())
            );

            if (macsMesh.stream().filter(seriales::contains).count() == 0) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_CON_ULTRAWIFI_SIN_AP_ESCLAVO_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_CON_ULTRAWIFI_SIN_AP_ESCLAVO_DESCRIPCION, transaction));
            }

            // 6) Buscar AP esclavo
            InventarioPorClienteDto meshSlave = HelperMesh.findInventarioCoincidente(equipos, macsMesh);
            if (meshSlave == null) {
                return HelperMesh.errorInventario(cuentaCliente, transaction);
            }

            // Consultar AP esclavo
            DeviceParamsDto dtoSlave = HelperMesh.buildDeviceParamsDto(
                    HelperMesh.serialPreferido(meshSlave),
                    Constantes.KEY_OR_TREE_MESH,
                    meshMaster.getMarca(),
                    meshMaster.getModelo()
            );

            DeviceParamsResponse responseSlave = consultarParametrosDipositivosService.consultarParametrosDispositivo(dtoSlave);
            if (HelperMesh.isAcsDataEmpty(responseSlave)) {
                return HelperMesh.diagnostico(cuentaCliente,
                        Constantes.ACS_NO_REPORTA_DATA_CODIGO,
                        Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION);
            }

            boolean wifiSlaveOk = canalWifiValidator.validar(responseSlave, dtoSlave.getKeyOrTree());

            // 7) Diagnósticos finales de canales
            if (!wifiSlaveOk) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_DESCRIPCION, transaction));
            }
            if (wifiOntOk && wifiSlaveOk) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_CON_ULTRAWIFI_CANALES_ONLINE_AP_ONT_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_CON_ULTRAWIFI_CANALES_ONLINE_AP_ONT_DESCRIPCION, transaction));
            }
            if (!wifiOntOk && wifiSlaveOk) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_ONT_ONLINE_AP_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.FTTH_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_ONT_ONLINE_AP_DESCRIPCION, transaction));
            }

            return HelperMesh.errorInventario(cuentaCliente, transaction);


        } catch (Exception e) {
            String codigo;
            String descripcion;
            try {
                codigo = ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO, transaction);
                descripcion = ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, transaction)
                        .replace("{}", cuentaCliente);
            } catch (Exception ex2) {
                // fallback por si falla la lectura del properties
                codigo = Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO;
                descripcion = Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION.replace("{}", cuentaCliente);
            }

            return new DiagnosticoResponse(
                    "OK",
                    ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                    List.of(new DiagnosticoDto(cuentaCliente, codigo, descripcion))
            );

        }
    }


}
