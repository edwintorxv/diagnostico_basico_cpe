package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultarParametrosDipositivos {

    private final IAcsPortOut iAcsPortOut;

    public DeviceParamsResponse consultarParametrosDispositivo(DeviceParamsDto deviceParamsDto) throws Exception {
        return iAcsPortOut.obtenerParametrosPorDispositivo(deviceParamsDto);
    }
}
