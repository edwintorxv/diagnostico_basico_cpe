package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

//@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor
public class ResponseCableModemDataDto {

    private List<CableModemDataDto> lstCableModemDto;

}
