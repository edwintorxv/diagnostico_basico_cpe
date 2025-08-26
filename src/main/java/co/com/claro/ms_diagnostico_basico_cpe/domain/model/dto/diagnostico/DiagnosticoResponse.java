package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.GenericResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticoResponse extends GenericResponseDto {

    private List<DiagnosticoDto> data;

    public DiagnosticoResponse(String status, String message, List<DiagnosticoDto> data) {
        super(status, message); // Llama al constructor de GenericResponseDto
        this.data = data;
    }
}
