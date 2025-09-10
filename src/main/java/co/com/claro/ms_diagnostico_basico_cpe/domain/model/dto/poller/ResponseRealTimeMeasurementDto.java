package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.GenericResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor
public class ResponseRealTimeMeasurementDto extends GenericResponseDto {

    public List<RealTimeMeasurementDto> lstRealTimeRealTimeMeasurement;

}
