package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFTTHResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;

import java.util.List;

public interface DiagnosticoStrategy {

    DiagnosticoFTTHResponse diagnosticar(String cuentaCliente,
                                         List<InventarioPorClienteDto> inventario,
                                         IAcsPortOut acsPortOut);

}
