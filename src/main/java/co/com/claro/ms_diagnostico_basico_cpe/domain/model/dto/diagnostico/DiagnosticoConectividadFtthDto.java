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
public class DiagnosticoConectividadFtthDto {

    private String idDiagnostico;
    private String descripcion;
    private String timeStamp;
    private String cuentaCliente;
    private String macAddress;
    private String marca;
    private String modelo;
    private String firmware;
    private String ipWAN;
    private String uptime;
    private boolean requiereReinicio;
    private boolean capacidadDiagnostico;
    private String estadoFinal;

    private PruebasConectividadDto pruebasConectividad;
    private AnalisisDto analisis;
    private List<AccionSugeridaDto> accionesSugeridas;

}
