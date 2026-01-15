package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PingCpeResponseDto {

    @JsonProperty("pingInfo")
    private PingCpeDto pingInfo;
    private String code;
    private String message;
    private String detailedMessage;
    private String type;

}