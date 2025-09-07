package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.GenericResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticoHfcLineaBaseResponse extends GenericResponseDto {

    private List<DiagnosticoHfcLineaBaseDto> data;

}
