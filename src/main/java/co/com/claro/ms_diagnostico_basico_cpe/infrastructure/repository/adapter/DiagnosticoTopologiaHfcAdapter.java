package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.repository.adapter;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico.IDiagnosticoHFCPortIn;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DiagnosticoTopologiaHfcAdapter {
	
	private final IDiagnosticoHFCPortIn diagnosticoBasicoPortIn;

    public ResponseEntity<DiagnosticoResponse> diagnosticoTopologiaHfc(String cuentaCliente) throws Exception {
        return ResponseEntity.ok(diagnosticoBasicoPortIn.diagnosticoTopologiaHfc(cuentaCliente));
    }

}
