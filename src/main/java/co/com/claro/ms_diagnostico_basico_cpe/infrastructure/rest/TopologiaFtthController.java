package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthResponse;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.repository.adapter.DiagnosticoTopologiaFtthAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constantes.REQUEST_MAPPING)
@RequiredArgsConstructor
public class TopologiaFtthController {

    private final DiagnosticoTopologiaFtthAdapter diagnosticoAdapter;

    @GetMapping("/topologiaONT/{cuentaCliente}")
    public ResponseEntity<DiagnosticoFtthResponse> diagnosticoTopologiaFtth(@PathVariable String cuentaCliente) throws Exception {
        return diagnosticoAdapter.diagnosticoTopologiaFtth(cuentaCliente);
    }
}
