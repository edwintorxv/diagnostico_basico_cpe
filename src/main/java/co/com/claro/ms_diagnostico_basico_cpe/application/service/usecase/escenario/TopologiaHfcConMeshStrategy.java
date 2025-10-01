package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.ParametersConfig;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import org.springframework.stereotype.Service;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.ConsultarParametrosDipositivosService;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.utils.HelperMesh;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.validator.CanalWifiValidator;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.validator.MacAdreesValidator;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseArpPollerDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseGetWifiData;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;

@Service
public class TopologiaHfcConMeshStrategy implements DiagnosticoTopologiaHfcStrategy {

    private final ConsultarParametrosDipositivosService consultarParametrosDipositivosService;
    private final MacAdreesValidator macAdreesValidator;
    private final CanalWifiValidator canalWifiValidator;

    public TopologiaHfcConMeshStrategy(CanalWifiValidator canalWifiValidator,
                                       ConsultarParametrosDipositivosService consultarParametrosDipositivosService,
                                       MacAdreesValidator macAdreesValidator) {
        this.canalWifiValidator = canalWifiValidator;
        this.consultarParametrosDipositivosService = consultarParametrosDipositivosService;
        this.macAdreesValidator = macAdreesValidator;
    }

    @Override
    public DiagnosticoFtthResponse diagnosticar(InventarioPorTopoligiaDto topologia, IPollerPortOut pollerPortOut,
                                                IAcsPortOut acsPortOut) throws Exception {

        String cuentaCliente = topologia.getCuentaCliente();
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

            List<String> seriales = equipos.stream().map(HelperMesh::serialInventarioNormalizado) // toma serialNumber o
                    // serialMac
                    .filter(Objects::nonNull).toList();
            List<ResponseArpPollerDto> listaArp = null;

            try {
                listaArp = pollerPortOut
                        .consultarARP(HelperMesh.formatMacAddress(topologia.getInventarioCPE().getSerialMac()));
            } catch (Exception e) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.HFC_ACS_NO_CUMPLE_ESTRUCTURA_ESPERADA_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.HFC_ACS_NO_CUMPLE_ESTRUCTURA_ESPERADA_MENSAJE, transaction));

            }

            if (listaArp == null || listaArp.isEmpty()) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_DESCRIPCION, transaction));
            }

            List<ResponseArpPollerDto> coincidencias = listaArp.stream().filter(arpItem -> {
                //boolean estadoActivo = "Activo".equalsIgnoreCase(arpItem.getStatus());
                String macArpSinFormato = arpItem.getMac().replace(":", "").toUpperCase();
                boolean macCoincide = seriales.contains(macArpSinFormato);
                //return estadoActivo && macCoincide;
                return macCoincide;
            }).collect(Collectors.toList());

            long numeroDeCoincidencias = coincidencias.size();

            if (numeroDeCoincidencias == 1) {

                ResponseArpPollerDto apMaestroEncontrado = coincidencias.get(0);
                String macMaster = apMaestroEncontrado.getMac();

                String macSinPuntos = HelperMesh.unformatMacAddress(macMaster);

                // 5) Consultar parámetros AP maestro
                Optional<InventarioPorClienteDto> maestroEncontradoInv = topologia.getLstinventarioMesh().stream()
                        .filter(equipo -> equipo.getSerialNumber() != null
                                && equipo.getSerialNumber().equals(macSinPuntos.toUpperCase()))
                        .findFirst();


                if (maestroEncontradoInv.isPresent()) {

                    InventarioPorClienteDto meshMaster = maestroEncontradoInv.get();

                    // Validar estado AP maestro en ACS
                    if (!HelperMesh.estaOnline(meshMaster.getSerialMac(), acsPortOut)) {
                        return HelperMesh.diagnostico(cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_CODIGO, transaction),
                                ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_DESCRIPCION, transaction));
                    }

                    // 5) Consultar parámetros AP maestro
                    DeviceParamsDto dtoMesh = HelperMesh.buildDeviceParamsDto(HelperMesh.serialPreferido(meshMaster),
                            Constantes.KEY_OR_TREE_MESH, HelperMesh.safeLower(meshMaster.getMarca()),
                            meshMaster.getModelo());

                    DeviceParamsResponse responseMesh = consultarParametrosDipositivosService
                            .consultarParametrosDispositivo(dtoMesh);

                    boolean wifiHFCOk = canalWifiValidator.validar(responseMesh, dtoMesh.getKeyOrTree());
                    boolean wifiCM;

                    String formatearMac = HelperMesh.formatMacAddress(topologia.getInventarioCPE().getSerialMac());

                    List<ResponseGetWifiData> getWifiData = pollerPortOut.consultarCMBandas(formatearMac);

                    if (getWifiData == null || getWifiData.isEmpty()) {
                        wifiCM = false;

                    } else {
                        wifiCM = getWifiData.stream()
                                .allMatch(wifi -> "true".equalsIgnoreCase(wifi.getEnableWireless()));
                    }

                    if (HelperMesh.isAcsDataEmpty(responseMesh)) {
                        return HelperMesh.diagnostico(cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.ACS_NO_REPORTA_DATA_CODIGO, transaction),
                                ParametersConfig.getPropertyValue(Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION, transaction));
                    }

                    List<String> macsMesh = HelperMesh.normalizarListaMac(
                            macAdreesValidator.macAddressDetectadas(responseMesh, dtoMesh.getKeyOrTree()));

                    if (macsMesh.stream().filter(seriales::contains).count() == 0) {
                        return HelperMesh.diagnostico(cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_SIN_AP_ESCLAVO_CODIGO, transaction),
                                ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_SIN_AP_ESCLAVO_DESCRIPCION, transaction));
                    }

                    if (wifiCM && wifiHFCOk) {
                        return HelperMesh.diagnostico(cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_CANALES_ONLINE_CODIGO, transaction),
                                ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_CANALES_ONLINE_DESCRIPCION, transaction));
                    } else if (!wifiCM && wifiHFCOk) {
                        return HelperMesh.diagnostico(cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_ONLINE_CM_CODIGO, transaction),
                                ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_ONLINE_CM_DESCRIPCION, transaction));
                    } else {
                        return HelperMesh.diagnostico(cuentaCliente,
                                ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_CODIGO, transaction),
                                ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_DESCRIPCION, transaction));
                    }
                }
            } else if (numeroDeCoincidencias > 1) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_DESCRIPCION, transaction));

            } else if (numeroDeCoincidencias == 0) {
                return HelperMesh.diagnostico(cuentaCliente,
                        ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_CODIGO, transaction),
                        ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_DESCRIPCION, transaction));
            }

            return HelperMesh.errorInventario(cuentaCliente, transaction);

        } catch (Exception e) {
            String codigo;
            String descripcion;
            try {
                codigo = ParametersConfig.getPropertyValue(Constantes.HFC_INVENTARIO_NO_ENCONTRADO_CODIGO, transaction);
                descripcion = ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, transaction)
                        .replace("{}", cuentaCliente);
            } catch (Exception ex2) {
                // fallback por si falla la lectura del properties
                codigo = Constantes.HFC_INVENTARIO_NO_ENCONTRADO_CODIGO;
                descripcion = Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION.replace("{}", cuentaCliente);
            }

            return new DiagnosticoFtthResponse(
                    "OK",
                    ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                    List.of(new DiagnosticoFtthDto(cuentaCliente, codigo, descripcion))
            );

        }

    }

}
