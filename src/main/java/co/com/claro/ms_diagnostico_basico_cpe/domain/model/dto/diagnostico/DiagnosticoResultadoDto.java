package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosticoResultadoDto {

    private String idDiagnostico;
    private String descripcion;
    private AnalisisDto analisisDto;
    private boolean requiereReinicio;
    private boolean capacidadDiagnostico;
    private List<AccionSugeridaDto> accionSugeridaDtoList;
    private String estadoFinal;

}
