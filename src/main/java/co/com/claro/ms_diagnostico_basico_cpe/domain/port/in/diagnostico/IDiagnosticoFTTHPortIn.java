package co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFTTHResponse;

public interface IDiagnosticoFTTHPortIn {

    DiagnosticoFTTHResponse diagnosticar(String cuentaCliente) throws Exception;

}
