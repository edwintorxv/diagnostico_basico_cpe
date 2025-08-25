package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import java.util.List;
import lombok.Data;

@Data
public class ResponseArpDto {
	
	private String status;
    private String message;
    private List<ResponseArpPollerDto> data;

}
