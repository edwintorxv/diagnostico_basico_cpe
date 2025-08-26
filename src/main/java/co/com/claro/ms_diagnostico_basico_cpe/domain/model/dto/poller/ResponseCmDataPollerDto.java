package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseCmDataPollerDto {
	
	@JsonAlias("alive")
	private String alive;
	
	@JsonAlias("brand")
	private String brand;
	
	@JsonAlias("buildingId")
	private String buildingId;
	
	@JsonAlias("buildingName")
	private String buildingName;
	
	@JsonAlias("clientAddress")
	private String clientAddress;
	
	@JsonAlias("cmts")
	private String cmts;
	
	@JsonAlias("cmtsInterface")
	private String cmtsInterface;
	
	@JsonAlias("cmtsIp")
	private String cmtsIp;
	
    @JsonAlias("division")
    private String division;
	
	@JsonAlias("docsCableMaclayerIndex")
	private String docsCableMaclayerIndex;
	
	@JsonAlias("firmware")
	private String firmware;
	
	@JsonAlias("index")
	private String index;
	
	@JsonAlias("interfaceIndex")
	private String interfaceIndex;
	
	@JsonAlias("ipAddress")
	private String ipAddress;
	
	@JsonAlias("lastSeen")
	private String lastSeen;
	
	@JsonAlias("mac")
	private String mac;
	
	@JsonAlias("matrixAccount")
	private String matrixAccount;
	
	@JsonAlias("model")
    private String model;
	
	@JsonAlias("node")
	private String node;
	
	@JsonAlias("rrAccount")
	private String rrAccount;
	
	@JsonAlias("standBy")
	private String standBy;
	
	@JsonAlias("status")
	private String status;
	
	@JsonAlias("sysDescr")
	private String sysDescr;
	
	@JsonAlias("uptime")
	private String uptime;
	
	@JsonAlias("validIndex")
	private String validIndex;

}
