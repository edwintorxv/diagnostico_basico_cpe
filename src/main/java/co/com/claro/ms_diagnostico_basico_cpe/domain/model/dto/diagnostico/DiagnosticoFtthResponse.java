package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.GenericResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticoFtthResponse extends GenericResponseDto {

    private List<DiagnosticoFtthDto> data;

    public DiagnosticoFtthResponse(String status, String message, List<DiagnosticoFtthDto> data) {
        super(status, message); // Llama al constructor de GenericResponseDto
        this.data = data;
    }
}
