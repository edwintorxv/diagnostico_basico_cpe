package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceStatusResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsultarEstadoDevice {

    private final IAcsPortOut iAcsPortOut;

    public DeviceStatusResponse validarEstado(String serial) throws Exception {
        return iAcsPortOut.obtenerEstadoPorSerial(serial);
    }

}
