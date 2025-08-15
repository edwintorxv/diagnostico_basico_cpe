package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 6/09/2024
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class GenericResponseDto {
    private String status; // Indica si la autenticación fue exitosa o fallida.
    private String message; // Mensaje descriptivo sobre el resultado de la operación.
}
