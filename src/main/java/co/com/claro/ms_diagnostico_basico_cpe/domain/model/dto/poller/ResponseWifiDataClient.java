package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import java.util.List;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.GenericResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString(callSuper = true) // <-- ¡Añade esto!
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseWifiDataClient extends GenericResponseDto {
	
	List<ResponseGetWifiData> data;

}
