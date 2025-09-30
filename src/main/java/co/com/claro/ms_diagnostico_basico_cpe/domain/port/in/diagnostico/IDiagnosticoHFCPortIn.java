package co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoHfcLineaBaseResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthResponse;

public interface IDiagnosticoHFCPortIn {
	
	DiagnosticoFtthResponse diagnosticoTopologiaHfc(String cuentaCliente) throws Exception;

	DiagnosticoHfcLineaBaseResponse diagnosticoLineaBaseHfc(String cuentaCliente) throws Exception;
}
