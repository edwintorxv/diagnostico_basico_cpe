package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFTTHResponse;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.adapter.DiagnosticoFTTHAdapter;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constantes.REQUEST_MAPPING)
@RequiredArgsConstructor
public class DiagnosticoFTTHController {

    private final DiagnosticoFTTHAdapter diagnosticoAdapter;

    @PostMapping("/diagnostico/{cuentaCliente}")
    public ResponseEntity<DiagnosticoFTTHResponse> diagnosticar(@PathVariable String cuentaCliente) throws Exception{
        return diagnosticoAdapter.diagnosticar(cuentaCliente);
    }
}
