package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.PingCpeDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.PingCpeResponseDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.TraceCpeHopDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.TraceCpeResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PruebasConectividadDto {

    private PingCpeResponseDto ping;
    private PingCpeDto pingFQDN;
    private TraceCpeResponseDto traceCpeResponseDto;

}
