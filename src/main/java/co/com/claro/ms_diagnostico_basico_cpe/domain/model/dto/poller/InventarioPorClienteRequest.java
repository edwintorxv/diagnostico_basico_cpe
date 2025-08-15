package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventarioPorClienteRequest {

    @JsonAlias("cuenta")
    @Schema(example = "1234567890")
    @NotBlank(message = "La cuenta es obligatoria")
    private String cuenta;

}
