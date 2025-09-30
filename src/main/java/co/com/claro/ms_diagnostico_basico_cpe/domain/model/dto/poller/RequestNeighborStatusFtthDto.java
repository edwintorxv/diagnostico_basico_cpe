package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestNeighborStatusFtthDto {

    private OntDataDto ontDataDto;
    private Integer samples;
    private Boolean geodistanceQuery;


}
