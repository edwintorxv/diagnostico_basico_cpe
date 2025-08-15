package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
public class DeviceParamsResponse {
    private String status;
    private String message;
    private List<DeviceData> data;
}

