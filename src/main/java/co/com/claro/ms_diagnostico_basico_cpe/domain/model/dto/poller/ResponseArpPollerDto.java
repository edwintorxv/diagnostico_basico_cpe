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
public class ResponseArpPollerDto {
	
	@JsonAlias("hostname")
	String hostname;
	
	@JsonAlias("ipAddress")
	String ipAddress;
	
	@JsonAlias("mac")
	String mac;
	
	@JsonAlias("status")
	String status;
	
	@JsonAlias("subNetMask")
	String subNetMask;

}
