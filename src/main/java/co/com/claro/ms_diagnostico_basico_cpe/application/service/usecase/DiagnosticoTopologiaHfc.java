package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.DiagnosticoTopologiaHfcStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.TopologiaHfcConMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario.TopologiaHfcSinMeshStrategy;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.utils.HelperMesh;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseCmDataPollerDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico.IDiagnosticoHFCPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class DiagnosticoTopologiaHfc implements IDiagnosticoHFCPortIn {
	
	private final IAcsPortOut acsPortOut;
    private final IPollerPortOut pollerPortOut;
    
    private final TopologiaHfcSinMeshStrategy hfcSinMeshsStrategy;
    private final TopologiaHfcConMeshStrategy hfcConMeshsStrategy;
    //private final InventarioPorTopoligiaDto inventarioPoller;
    private final InventarioPoller inventarioPoller;

	@Override
	public DiagnosticoResponse diagnosticoTopologiaHfc(String cuentaCliente) throws Exception {
		
		
		 InventarioPorTopoligiaDto inventarioTopologiaHfc =
	                inventarioPoller.consultarInventario(cuentaCliente, "hfc");

	        if (inventarioTopologiaHfc == null || inventarioTopologiaHfc.getInventarioCPE() == null) {
	            return new DiagnosticoResponse(
	                    "OK",
	                    ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
	                    List.of(new DiagnosticoDto(
	                            cuentaCliente,
	                            Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO,
	                            String.format(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, cuentaCliente)
	                    ))
	            );
	        }
	        
	        //Validar si el cable Modem esta conectado  
	        String formatearMac = HelperMesh.formatMacAddress(inventarioTopologiaHfc.getInventarioCPE().getSerialMac());
	        List<ResponseCmDataPollerDto> getCmData = pollerPortOut.consultarCMData(formatearMac);
			 
			 if (getCmData == null || getCmData.isEmpty()) {
				 return HelperMesh.diagnostico(cuentaCliente,
	                     "300",
	                     "No es posible consultar el CM");
		     }
			 
			 ResponseCmDataPollerDto getEstadoBandas = (ResponseCmDataPollerDto) getCmData.get(0);
			 
			 if ("false".equalsIgnoreCase(getEstadoBandas.getAlive())) {
					return HelperMesh.diagnostico(cuentaCliente, "300",
							"No es posible consultar el CM");
				 
			 }
		
	        
	        DiagnosticoTopologiaHfcStrategy strategy =
	                (inventarioTopologiaHfc.getLstinventarioMesh() == null || inventarioTopologiaHfc.getLstinventarioMesh().isEmpty())
	                        ? hfcSinMeshsStrategy
	                        : hfcConMeshsStrategy;

	        return strategy.diagnosticar(inventarioTopologiaHfc, pollerPortOut, acsPortOut);
		
	}
	
	

}
