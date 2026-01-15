package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TraceCpeDto {

    private String cpeInterface;
    private String host;
    private String numberOfTries;
    private String dataBlockSize;
    private String timeout;
    private String maxHopCount;
    private String routeHopsNumberOfEntries;
    private List<TraceCpeHopDto> hops;

}
