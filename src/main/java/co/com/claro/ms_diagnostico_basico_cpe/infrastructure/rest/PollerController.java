package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest;


import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.InventarioPorClienteService;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteRequest;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteResponse;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constantes.REQUEST_MAPPING)
public class PollerController {

    private final InventarioPorClienteService inventarioPorClienteService;


    @PostMapping("/consultaInventario")
    public InventarioPorClienteResponse consultar(@RequestBody InventarioPorClienteRequest request) throws Exception{
        return inventarioPorClienteService.consultarInventarioPorCliente(request);
    }

}
