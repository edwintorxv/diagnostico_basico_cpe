package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico;

import java.util.List;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.GenericResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticoHfcResponse extends GenericResponseDto {
	
	  private List<DiagnosticoHfcDto> data;

	    public DiagnosticoHfcResponse(String status, String message, List<DiagnosticoHfcDto> data) {
	        super(status, message);
	        this.data = data;
	    }
}
