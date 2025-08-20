package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.DiagnosticoTopologiaFtthStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.TopologiaFtthConMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.TopologiaFtthSinMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteRequest;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico.IDiagnosticoFTTHPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosticoTopologiaFtth implements IDiagnosticoFTTHPortIn {

    private final IAcsPortOut acsPortOut;
    private final IPollerPortOut pollerPortOut;

    private final TopologiaFtthSinMeshStrategy ftthSinMeshsStrategy;
    private final TopologiaFtthConMeshStrategy ftthConMeshsStrategy;

    @Override
    public DiagnosticoFtthResponse diagnosticoTopologiaFtth(String cuentaCliente) throws Exception {

        InventarioPorClienteRequest inventarioRequest = new InventarioPorClienteRequest();
        inventarioRequest.setCuenta(cuentaCliente);

        InventarioPorClienteResponse inventarioPorClienteResponse =
                pollerPortOut.obtenerInventarioPorCliente(inventarioRequest);

        if (inventarioPorClienteResponse == null || inventarioPorClienteResponse.getData().isEmpty()) {
            return new DiagnosticoFtthResponse(
                    "OK",
                    ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
                    List.of(new DiagnosticoFtthDto(
                            cuentaCliente,
                            Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO,
                            Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION
                    ))
            );
        }

        List<InventarioPorClienteDto> inventarioCliente = inventarioPorClienteResponse.getData();

        List<InventarioPorClienteDto> cpePrincipal = inventarioCliente.stream()
                .filter(i -> "ftth".equalsIgnoreCase(i.getProducto()))
                .toList();

        if (cpePrincipal.isEmpty()) {
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

        List<InventarioPorClienteDto> equiposAdicionales = inventarioCliente.stream()
                .filter(i -> !"ftth".equalsIgnoreCase(i.getProducto()))
                .toList();

        DiagnosticoTopologiaFtthStrategy strategy = equiposAdicionales.isEmpty()
                ? ftthSinMeshsStrategy
                : ftthConMeshsStrategy;

        return strategy.diagnosticar(cuentaCliente, inventarioCliente, acsPortOut);
    }
}
