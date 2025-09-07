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
public class ResponseGetWifiData {
	
	@JsonAlias("channel")
	String channel;
	
	@JsonAlias("dfsEnabled")
	String dfsEnabled;
	
	@JsonAlias("enableWireless")
	String enableWireless;
	
	@JsonAlias("encriptionMode")
	String encriptionMode;
	
	@JsonAlias("key")
	String key;
	
	@JsonAlias("mac")
	String mac;
	
	@JsonAlias("ssid")
	String ssid;

}
