package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;

import lombok.Data;

@Data
public class DeviceParam {
    private String errorCode;
    private String message;
    private String name;
    private String value;
}
