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
public class PingCpeDto {

    @JsonProperty("AverageResponseTime")
    private String averageResponseTime;
    @JsonProperty("FailureCount")
    private String failureCount;
    @JsonProperty("MaximumResponseTime")
    private String maximumResponseTime;
    @JsonProperty("MinimumResponseTime")
    private String minimumResponseTime;
    @JsonProperty("SuccessCount")
    private String successCount;

}
