package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventarioPorTopoligiaDto {

    InventarioPorClienteDto inventarioCPE;

    List<InventarioPorClienteDto> lstinventarioMesh;

    String cuentaCliente;

}
