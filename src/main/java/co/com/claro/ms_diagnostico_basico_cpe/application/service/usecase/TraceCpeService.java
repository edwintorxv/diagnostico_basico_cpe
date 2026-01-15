package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.PingCpeRequestDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.PingCpeResponseDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.TraceCpeRequestDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.TraceCpeResponseDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TraceCpeService {

    private final IAcsPortOut iAcsPortOut;

    public TraceCpeResponseDto dataTraceCpe(TraceCpeRequestDto traceCpeRequestDto) throws Exception {
        return iAcsPortOut.obtenerResultadoTrace(traceCpeRequestDto);
    }
}
