package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TraceCpeHopDto {

    @JsonProperty("hop")
    private String hop;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("host")
    private String host;

    @JsonProperty("hostAddress")
    private String hostAddress;

    @JsonProperty("rTTimes")
    private String rTTimes;

}
