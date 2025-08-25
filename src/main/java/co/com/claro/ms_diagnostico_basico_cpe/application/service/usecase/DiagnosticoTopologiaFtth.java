package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.ITopologiaFtthConMeshStrategyPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.ITopologiaFtthSinMeshStrategyPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico.IDiagnosticoFTTHPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class DiagnosticoTopologiaFtth implements IDiagnosticoFTTHPortIn {

    private final IAcsPortOut acsPortOut;
    private final IPollerPortOut pollerPortOut;

    private final ITopologiaFtthSinMeshStrategyPortIn ftthSinMeshsStrategy;
    private final ITopologiaFtthConMeshStrategyPortIn ftthConMeshsStrategy;
    //private final InventarioPorTopoligiaDto inventarioPoller;
    private final InventarioPoller inventarioPoller;

    @Override
    public DiagnosticoFtthResponse diagnosticoTopologiaFtth(String cuentaCliente) throws Exception {


        InventarioPorTopoligiaDto inventarioTopologiaFtth =
                inventarioPoller.consultarInventario(cuentaCliente, "ftth");

        if (inventarioTopologiaFtth == null || inventarioTopologiaFtth.getInventarioCPE() == null) {
            return new DiagnosticoFtthResponse(
                    "OK",
                    ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                    List.of(new DiagnosticoFtthDto(
                            cuentaCliente,
                            Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO,
                            String.format(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, cuentaCliente)
                    ))
            );
        }

        if (inventarioTopologiaFtth.getLstinventarioMesh() == null || inventarioTopologiaFtth.getLstinventarioMesh().isEmpty()) {
            return ftthSinMeshsStrategy.diagnosticar(inventarioTopologiaFtth);
        } else {
            return ftthConMeshsStrategy.diagnosticar(inventarioTopologiaFtth);
        }
    }
}
