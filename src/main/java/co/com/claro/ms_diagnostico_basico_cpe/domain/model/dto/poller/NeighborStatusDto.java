package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NeighborStatusDto {

    private String alive;
    private String buildingId;
    private String buildingName;
    private String clientAddress;
    private String cmts;
    private String cmtsInterface;
    private String cmtsIp;
    private String division;
    private String index;
    private String interfaceIndex;
    private String ipAddress;
    private String lastSeen;
    private String mac;
    private String matrixAccount;
    private String neighborMethod;
    private String node;
    private String rrAccount;
    private String rx;
    private String snrDw;
    private String snrUp;
    private String status;
    private String timestamp;
    private String tx;
    private String upCorrectedCodewords;
    private String upUnErrorCodewords;
    private String upUncorrectableCodewords;

}
