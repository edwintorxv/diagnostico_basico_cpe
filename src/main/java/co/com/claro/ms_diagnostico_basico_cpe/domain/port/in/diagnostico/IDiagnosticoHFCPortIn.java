package co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoHfcLineaBaseResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;

public interface IDiagnosticoHFCPortIn {
	
	DiagnosticoResponse diagnosticoTopologiaHfc(String cuentaCliente) throws Exception;

	DiagnosticoResponse diagnosticoLineaBaseHfc(String cuentaCliente) throws Exception;
}
