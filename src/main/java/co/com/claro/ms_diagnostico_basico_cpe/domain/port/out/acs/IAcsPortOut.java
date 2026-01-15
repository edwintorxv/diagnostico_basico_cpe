package co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.*;

public interface IAcsPortOut {

    DeviceStatusResponse obtenerEstadoPorSerial(String serial) throws Exception;

    DeviceParamsResponse obtenerParametrosPorDispositivo(DeviceParamsDto dto) throws Exception;

    PingCpeResponseDto obtenerResultadoPingCPE(PingCpeRequestDto pingCpeRequestDto) throws Exception;

    TraceCpeResponseDto obtenerResultadoTrace(TraceCpeRequestDto traceCpeRequestDto) throws Exception;



}


