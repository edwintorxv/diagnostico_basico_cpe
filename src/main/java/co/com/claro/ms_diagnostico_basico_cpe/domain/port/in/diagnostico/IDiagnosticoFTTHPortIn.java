package co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthResponse;

public interface IDiagnosticoFTTHPortIn {

    DiagnosticoFtthResponse diagnosticoTopologiaFtth(String cuentaCliente) throws Exception;

}
