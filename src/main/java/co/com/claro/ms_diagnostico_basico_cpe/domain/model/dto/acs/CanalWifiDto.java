package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CanalWifiDto {

    private Integer canal24GHz;
    private Integer canal5GHz;
}
