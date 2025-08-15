package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceParamsRequest {

    private String devicesn;
    private String source;
    private String OUI;
    private String modelname;
    private boolean names;
    private boolean values;
    private boolean attributes;
    private String creator;
    private String appid;
    private String keyOrTree;

}
