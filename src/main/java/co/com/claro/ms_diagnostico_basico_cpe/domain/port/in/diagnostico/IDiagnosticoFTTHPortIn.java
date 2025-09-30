package co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthLineaBaseDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.ResponseDiagnosticoFtthLineaBaseDto;

public interface IDiagnosticoFTTHPortIn {

    DiagnosticoFtthResponse diagnosticoTopologiaFtth(String cuentaCliente) throws Exception;

    ResponseDiagnosticoFtthLineaBaseDto diagnosticoLineaBase(String cuenta) throws Exception;

}
