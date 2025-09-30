package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NeighborStatusFtthDto {

    private String alive;
    private String buildingId;
    private String buildingName;
    private String clientAddress;
    private boolean foundInACS;
    private String firmware;
    private String gPonPort;
    private String mac;
    private String matrixAccount;
    private int neighborMethod;
    private String olt;
    private String rrAccount;
    private double rx;
    private String serial;
    private String timestamp;
    private String tx;

}
