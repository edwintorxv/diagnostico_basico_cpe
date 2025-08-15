package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest;


import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.InventarioPorClienteService;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteRequest;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diagnosticoBasicoCPE")
public class InventarioPorCLienteController {

    private final InventarioPorClienteService inventarioPorClienteService;


    @PostMapping("/diagnosticoOnt")
    public InventarioPorClienteResponse consultar(@RequestBody InventarioPorClienteRequest request) throws Exception{
        return inventarioPorClienteService.consultarInventarioPorCliente(request);
    }

}
