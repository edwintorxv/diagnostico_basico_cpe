package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventarioPorClienteDto {

    @JsonAlias("cuenta")
    private String cuenta;

    @JsonAlias("serial_mac")
    private String serialMac;

    @JsonAlias("serial_number")
    private String serialNumber;

    @JsonAlias("producto")
    private String producto;

    @JsonAlias("marca")
    private String marca;

    @JsonAlias("modelo")
    private String modelo;

}
