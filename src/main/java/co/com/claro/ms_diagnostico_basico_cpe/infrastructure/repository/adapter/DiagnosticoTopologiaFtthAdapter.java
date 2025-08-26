package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.repository.adapter;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico.IDiagnosticoFTTHPortIn;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiagnosticoTopologiaFtthAdapter {

    private final IDiagnosticoFTTHPortIn diagnosticoBasicoPortIn;

    public ResponseEntity<DiagnosticoResponse> diagnosticoTopologiaFtth(String cuentaCliente) throws Exception {
        return ResponseEntity.ok(diagnosticoBasicoPortIn.diagnosticoTopologiaFtth(cuentaCliente));
    }

}
