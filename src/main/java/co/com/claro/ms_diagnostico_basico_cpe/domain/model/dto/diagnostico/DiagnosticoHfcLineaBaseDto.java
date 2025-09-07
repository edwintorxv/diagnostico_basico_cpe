package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticoHfcLineaBaseDto {

    private String idDiagnostico;
    private String descripcion;
    private String cuentaCliente;
    private String estadoOnt;
    private String estadoVecinos;

}
