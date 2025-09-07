package co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoHfcLineaBaseResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;
import org.springframework.http.ResponseEntity;

public interface IDiagnosticoHFCPortIn {
	
	DiagnosticoResponse diagnosticoTopologiaHfc(String cuentaCliente) throws Exception;

	DiagnosticoHfcLineaBaseResponse diagnosticoLineaBaseHfc(String cuentaCliente) throws Exception;
}
