package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraceCpeResponseDto {

    private TraceCpeDto traceInfo;
    private String code;
    private String message;
    private String detailedMessage;
    private String type;


}
