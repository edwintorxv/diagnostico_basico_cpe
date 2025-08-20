package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;

import java.util.List;

public interface DiagnosticoTopologiaFtthStrategy {

    DiagnosticoFtthResponse diagnosticar(String cuentaCliente,
                                         List<InventarioPorClienteDto> inventario,
                                         IAcsPortOut acsPortOut);

}
