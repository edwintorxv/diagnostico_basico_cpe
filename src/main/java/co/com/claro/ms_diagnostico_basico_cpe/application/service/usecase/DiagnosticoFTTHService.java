package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.DiagnosticoStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.FtthConMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.FtthSinMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFTTHResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteRequest;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico.IDiagnosticoFTTHPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiagnosticoFTTHService implements IDiagnosticoFTTHPortIn {

    private final IAcsPortOut acsPortOut;
    private final IPollerPortOut pollerPortOut;

    private final FtthSinMeshStrategy ftthSinMeshsStrategy;
    private final FtthConMeshStrategy ftthConMeshsStrategy;

    @Override
    public DiagnosticoFTTHResponse diagnosticar(String cuentaCliente) throws Exception {

        InventarioPorClienteRequest inventarioRequest = new InventarioPorClienteRequest();
        inventarioRequest.setCuenta(cuentaCliente);

        InventarioPorClienteResponse inventarioPorClienteResponse =
                pollerPortOut.obtenerInventarioPorCliente(inventarioRequest);

        if (inventarioPorClienteResponse == null || inventarioPorClienteResponse.getData().isEmpty()) {
            return new DiagnosticoFTTHResponse(
                    cuentaCliente,
                    Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO,
                    Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION
            );
        }

        List<InventarioPorClienteDto> inventarioCliente = inventarioPorClienteResponse.getData();

        List<InventarioPorClienteDto> cpePrincipal = inventarioCliente.stream()
                .filter(i -> "ftth".equalsIgnoreCase(i.getProducto()))
                .toList();

        if (cpePrincipal.isEmpty()) {
            return new DiagnosticoFTTHResponse(
                    cuentaCliente,
                    Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO,
                    Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION
            );
        }

        List<InventarioPorClienteDto> equiposAdicionales = inventarioCliente.stream()
                .filter(i -> !"ftth".equalsIgnoreCase(i.getProducto()))
                .toList();

        DiagnosticoStrategy strategy = equiposAdicionales.isEmpty()
                ? ftthSinMeshsStrategy
                : ftthConMeshsStrategy;

        return strategy.diagnosticar(cuentaCliente, inventarioCliente, acsPortOut);
    }
}
