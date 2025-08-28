package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.DiagnosticoTopologiaFtthStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.TopologiaFtthConMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.TopologiaFtthSinMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteRequest;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico.IDiagnosticoFTTHPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.ParametersConfig;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosticoTopologiaFtth implements IDiagnosticoFTTHPortIn {

    Transaction transaction = Transaction.startTransaction();

    private final IAcsPortOut acsPortOut;
    private final IPollerPortOut pollerPortOut;


    private final TopologiaFtthSinMeshStrategy ftthSinMeshsStrategy;
    private final TopologiaFtthConMeshStrategy ftthConMeshsStrategy;
    //private final InventarioPorTopoligiaDto inventarioPoller;
    private final InventarioPoller inventarioPoller;

    @Override
    public DiagnosticoResponse diagnosticoTopologiaFtth(String cuentaCliente) throws Exception {


        InventarioPorTopoligiaDto inventarioTopologiaFtth =
                inventarioPoller.consultarInventario(cuentaCliente, "ftth");

        if (inventarioTopologiaFtth == null || inventarioTopologiaFtth.getInventarioCPE() == null) {
            return new DiagnosticoResponse(
                    "OK",
                    ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                    List.of(new DiagnosticoDto(
                            cuentaCliente,
                            ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO, transaction),
                            ParametersConfig.getPropertyValue(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, transaction)
                                    .replace("{}", cuentaCliente)
                    ))
            );
        }

        DiagnosticoTopologiaFtthStrategy strategy =
                (inventarioTopologiaFtth.getLstinventarioMesh() == null || inventarioTopologiaFtth.getLstinventarioMesh().isEmpty())
                        ? ftthSinMeshsStrategy
                        : ftthConMeshsStrategy;

        return strategy.diagnosticar(inventarioTopologiaFtth, acsPortOut);
    }
}
