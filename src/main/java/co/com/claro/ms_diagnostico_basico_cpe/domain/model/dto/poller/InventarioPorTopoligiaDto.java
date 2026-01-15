package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventarioPorTopoligiaDto {

    private InventarioPorClienteDto inventarioCPE;

    @Builder.Default
    private List<InventarioPorClienteDto> lstinventarioMesh = new ArrayList<>();

    private String cuentaCliente;
}
