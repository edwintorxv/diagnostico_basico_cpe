package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import java.util.List;

import lombok.Data;

@Data
public class ResponseCmDataDto {
	
	private String status;
    private String message;
    private List<ResponseCmDataPollerDto> data;

}
