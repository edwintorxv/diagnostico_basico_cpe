package co.com.claro.ms_diagnostico_basico_cpe.domain.port.in;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;

public interface ITopologiaFtthSinMeshStrategyPortIn {
    DiagnosticoFtthResponse diagnosticar(InventarioPorTopoligiaDto inventario) throws Exception;
}
