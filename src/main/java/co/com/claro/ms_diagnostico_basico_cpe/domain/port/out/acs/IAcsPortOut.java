package co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceStatusResponse;

public interface IAcsPortOut {

    DeviceStatusResponse obtenerEstadoPorSerial(String serial) throws Exception;

    DeviceParamsResponse obtenerParametrosPorDispositivo(DeviceParamsDto dto) throws Exception;
}


