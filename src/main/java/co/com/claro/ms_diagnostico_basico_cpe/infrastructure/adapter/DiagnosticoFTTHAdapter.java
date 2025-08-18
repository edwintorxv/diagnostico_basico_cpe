package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.adapter;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFTTHResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico.IDiagnosticoFTTHPortIn;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiagnosticoFTTHAdapter {

    private final IDiagnosticoFTTHPortIn diagnosticoBasicoPortIn;

    public ResponseEntity<DiagnosticoFTTHResponse> diagnosticar(String cuentaCliente) throws Exception {
        return ResponseEntity.ok(diagnosticoBasicoPortIn.diagnosticar(cuentaCliente));
    }

}
