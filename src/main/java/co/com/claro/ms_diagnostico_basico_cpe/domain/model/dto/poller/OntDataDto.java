package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OntDataDto {

    private String alive;
    private String buildingId;
    private String buildingName;
    private String clientAddress;
    private String docsCableMaclayerIndex;
    private String firmware;
    private String gPonPort;
    private String internetServiceCode;
    private String mac;
    private String matrixAccount;
    private String model;
    private String nodo;
    private String olt;
    private String oltCard;
    private String oltIp;
    private String oltPort;
    private String rrAccount;
    private String serial;
    private String telephonyServiceCode;
    private String uptime;
    private String vendor;

}
