package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IpOntDto {

    private String gestion;
    private String gestionDNS;
    private String gestionEstado;
    private String internet;
    private String internetDNS;
    private String internetEstado;
    private String telefonia;
    private String telefoniaDNS;
    private String telefoniaEstado;
    private String television;
    private String televisionDNS;
    private String televisionEstado;


}
