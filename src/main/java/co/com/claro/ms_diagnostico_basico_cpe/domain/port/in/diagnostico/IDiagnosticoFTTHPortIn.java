package co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;

public interface IDiagnosticoFTTHPortIn {

    DiagnosticoResponse diagnosticoTopologiaFtth(String cuentaCliente) throws Exception;




}
