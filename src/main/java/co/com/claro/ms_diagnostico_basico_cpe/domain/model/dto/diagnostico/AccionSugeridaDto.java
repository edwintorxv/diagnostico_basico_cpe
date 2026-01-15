package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccionSugeridaDto {

    private String tipo;
    private String nivel;
    private String motivo;

}
