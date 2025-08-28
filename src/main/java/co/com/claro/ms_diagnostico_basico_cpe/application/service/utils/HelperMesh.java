package co.com.claro.ms_diagnostico_basico_cpe.application.service.utils;

import java.util.List;
import java.util.Objects;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceStatusResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.ParametersConfig;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;

public class HelperMesh {

    private HelperMesh() {
        // Constructor privado para prevenir la instanciación.
    }


    public static DiagnosticoResponse errorInventario(String cuentaCliente, Transaction transaction) throws Exception {
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

    public static DeviceParamsDto buildDeviceParamsDto(String serial, String keyTemplate, String marca, String modelo) {
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

    public static DiagnosticoResponse diagnostico(String cuenta, String codigo, String descripcion) {
        return new DiagnosticoResponse("OK",
                ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                List.of(new DiagnosticoDto(
                        cuenta,
                        codigo,
                        descripcion
                ))
        );
    }

    public static InventarioPorClienteDto findInventarioCoincidente(List<InventarioPorClienteDto> inventario, List<String> macs) {
        return inventario.stream()
                .filter(i -> i.getSerialNumber() != null)
                .filter(i -> macs.contains(i.getSerialNumber().replace(":", "").toUpperCase()))
                .findFirst()
                .orElse(null);
    }

    public static boolean estaOnline(String serial, IAcsPortOut acsPortOut) throws Exception {
        DeviceStatusResponse status = acsPortOut.obtenerEstadoPorSerial(serial);
        return status.getData() != null &&
                status.getData().stream().anyMatch(d -> "true".equalsIgnoreCase(d.getOnline()));
    }

    public static boolean isAcsDataEmpty(DeviceParamsResponse response) {
        return response == null
                || response.getData() == null
                || response.getData().isEmpty()
                || response.getData().get(0).getParams() == null
                || response.getData().get(0).getParams().isEmpty();
    }

    public static String norm(String s) {
        return (s == null) ? null : s.replace(":", "").toUpperCase();
    }

    public static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    public static String serialInventarioNormalizado(InventarioPorClienteDto i) {
        String raw = (i.getSerialNumber() != null && !i.getSerialNumber().isEmpty())
                ? i.getSerialNumber()
                : i.getSerialMac();
        return norm(raw);
    }

    public static List<String> normalizarListaMac(List<String> macs) {
        if (macs == null) return List.of();
        // La llamada this::norm se convierte en HelperMesh::norm
        return macs.stream().map(HelperMesh::norm).filter(Objects::nonNull).toList();
    }

    public static String serialPreferido(InventarioPorClienteDto i) {
        return (i.getSerialMac() != null && !i.getSerialMac().isEmpty())
                ? i.getSerialMac()
                : i.getSerialNumber();
    }

    public static String formatMacAddress(String macSinFormato) {

        if (macSinFormato == null || macSinFormato.length() != 12) {
            return macSinFormato;
        }

        return macSinFormato.replaceAll("(.{2})", "$1:").substring(0, 17);
    }

    public static String unformatMacAddress(String macConFormato) {
        if (macConFormato == null) {
            return null;
        }

        return macConFormato.replace(":", "");
    }

}
