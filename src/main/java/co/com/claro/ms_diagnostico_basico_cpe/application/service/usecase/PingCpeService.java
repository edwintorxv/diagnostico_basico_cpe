package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.PingCpeDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.PingCpeRequestDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.PingCpeResponseDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PingCpeService {

    private final IAcsPortOut iAcsPortOut;

    public PingCpeResponseDto dataPingCpe(PingCpeRequestDto pingCpeRequestDto) throws Exception {
        return iAcsPortOut.obtenerResultadoPingCPE(pingCpeRequestDto);
    }
}
