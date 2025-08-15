package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;

import lombok.Data;

import java.util.List;

@Data
public class DeviceData {
    private List<DeviceParam> params;
}
