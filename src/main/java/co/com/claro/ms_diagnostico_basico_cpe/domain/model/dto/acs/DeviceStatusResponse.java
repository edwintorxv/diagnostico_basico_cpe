package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;

import lombok.Data;

import java.util.List;

@Data
public class DeviceStatusResponse {
    private String status;
    private String message;
    private List<DeviceStatusDto> data;
}
