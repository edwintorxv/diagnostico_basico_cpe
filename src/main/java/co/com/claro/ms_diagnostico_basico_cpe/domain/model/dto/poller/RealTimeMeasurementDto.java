package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RealTimeMeasurementDto {

    private String downTraffic;
    private String rx;
    private String snrDw;
    private String snrUp;
    private String timestamp;
    private String tx;
    private String upCorrectedCodewords;
    private String upTraffic;
    private String upUnErrorCodewords;
    private String upUncorrectableCodewords;

}
