package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;


import lombok.Data;

@Data
public class DeviceStatusDto {

    private String errorCode;
    private String message;
    private String online;

}
