package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFTTHResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FtthConMeshStrategy implements DiagnosticoStrategy {

    @Override
    public DiagnosticoFTTHResponse diagnosticar(String cuentaCliente, List<InventarioPorClienteDto> inventario, IAcsPortOut acsPortOut) {

        //Borrar cuando implmente lógica
        return new DiagnosticoFTTHResponse(
                "111111",
                Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO,
                Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION
        );
    }
}
