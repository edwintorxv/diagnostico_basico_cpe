package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.repository.adapter.DiagnosticoTopologiaFtthAdapter;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.repository.adapter.DiagnosticoTopologiaHfcAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(Constantes.REQUEST_MAPPING)
@RequiredArgsConstructor
public class TopologiaHfcController {
		
	private final DiagnosticoTopologiaHfcAdapter diagnosticoAdapter;
	
	
	@GetMapping("/topologiaHFC/{cuentaCliente}")
    public ResponseEntity<DiagnosticoResponse> diagnosticoTopologiaHfc(@PathVariable String cuentaCliente) throws Exception{
        return diagnosticoAdapter.diagnosticoTopologiaHfc(cuentaCliente);
    }
	
}
