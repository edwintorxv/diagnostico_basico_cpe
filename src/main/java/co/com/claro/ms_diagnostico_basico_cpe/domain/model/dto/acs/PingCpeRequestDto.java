package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PingCpeRequestDto {

    private String originId;
    private String serial;
    private String account;
    private String vendor;
    private String model;
    private String maxHopCount;
    private String timeout;
    private String fqdn;

}
