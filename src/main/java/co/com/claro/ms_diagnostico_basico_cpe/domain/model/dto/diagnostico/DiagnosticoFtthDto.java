package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticoFtthDto {

    private String account;
    private String code;
    private String description;
}
