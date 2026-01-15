package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModeloSoportePruebasDto {

    private int id;
    private String marca;
    private String modelo;
    private String pingIp;
    private String pingFqdn;
    private String trazaIp;
    private String trazaFqdn;


}
