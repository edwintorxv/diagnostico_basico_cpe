package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProvisioningDataDto {

    private String iwDataPlan;
    private String iwDownstream;
    private String iwMacMtas;
    private String iwPhones;
    private String iwRrAccount;
    private String iwShortPlan;
    private String iwUpstream;
    private String rrAccount;
    private String rrMacMta;
    private String rrNode;
    private String rrPhones;
    private String rrService;
    private String rrTelService;


}
