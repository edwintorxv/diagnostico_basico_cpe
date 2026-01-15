package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalisisDto {

    private String condicion;
    private String conclusion;

}
