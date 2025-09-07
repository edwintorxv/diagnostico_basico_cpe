package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CableModemDataDto {

    private String fabricante;
    private String modelo;
    private String docsis2;
    private String wifi;
    private String servicio;
    private String dobleBanda;
    private String cambiar;
    private String ofrecerMesh;
    private String velocidadMaxima;

}
