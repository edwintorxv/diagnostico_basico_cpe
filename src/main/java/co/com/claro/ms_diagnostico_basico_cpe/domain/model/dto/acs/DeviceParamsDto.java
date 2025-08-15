package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceParamsDto {

    private String devicesn;
    private String source;
    @JsonProperty("OUI")
    private String OUI;
    private String modelname;
    private boolean names;
    private boolean values;
    private boolean attributes;
    private String creator;
    private String appid;
    private String keyOrTree;
}
