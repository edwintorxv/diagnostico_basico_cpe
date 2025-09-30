package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteRequest;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventarioPoller {

    private final IPollerPortOut pollerPortOut;

    public InventarioPoller(IPollerPortOut pollerPortOut) {
        this.pollerPortOut = pollerPortOut;
    }

    public InventarioPorTopoligiaDto consultarInventario(String cuentaCliente, String tecnologia) throws Exception {

        InventarioPorClienteRequest inventarioRequest = new InventarioPorClienteRequest();
        inventarioRequest.setCuenta(cuentaCliente);

        InventarioPorClienteResponse inventarioPorClienteResponse =
                pollerPortOut.obtenerInventarioPorCliente(inventarioRequest);

        if (inventarioPorClienteResponse == null || inventarioPorClienteResponse.getData().isEmpty()) {
            return null;
        }

        List<InventarioPorClienteDto> lstinventarioMesh = new ArrayList<>();
        InventarioPorTopoligiaDto inventarioPorTopoligiaDto = new InventarioPorTopoligiaDto();

        inventarioPorClienteResponse.getData().forEach(registro -> {
            if (registro.getProducto().equalsIgnoreCase(tecnologia)) {
                inventarioPorTopoligiaDto.setInventarioCPE(registro);
                inventarioPorTopoligiaDto.setCuentaCliente(cuentaCliente);
            } else if (registro.getProducto().toLowerCase().contains("eshs")) {
                lstinventarioMesh.add(registro);
            }
        });

        if (inventarioPorTopoligiaDto.getInventarioCPE() != null && lstinventarioMesh.size() > 0) {
            inventarioPorTopoligiaDto.setLstinventarioMesh(lstinventarioMesh);
        }

        return inventarioPorTopoligiaDto;
    }

}
