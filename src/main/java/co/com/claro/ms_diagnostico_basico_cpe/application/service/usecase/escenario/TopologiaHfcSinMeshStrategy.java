package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario;

import java.util.List;

import org.springframework.stereotype.Service;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.utils.HelperMesh;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseGetWifiData;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;

@Service
public class TopologiaHfcSinMeshStrategy implements DiagnosticoTopologiaHfcStrategy {

	
	//diagnosticar
	@Override
	public DiagnosticoResponse diagnosticar(InventarioPorTopoligiaDto inventario, IPollerPortOut pollerPortOut, IAcsPortOut acsPortOut) throws Exception {
		
		 String cuentaCliente = inventario.getCuentaCliente();
		 
		 String formatearMac = HelperMesh.formatMacAddress(inventario.getInventarioCPE().getSerialMac());
	
		 List<ResponseGetWifiData> getWifiData = pollerPortOut.consultarCMBandas(formatearMac);
		 
		 
		 if ("false".equalsIgnoreCase(getWifiData.get(0).getEnableWireless())) {
				return HelperMesh.diagnostico(cuentaCliente, "302",
						"Canales deshabilitados en CM. Se debe validar con cliente cuál es su topología");
			 
		 }else {
			 return HelperMesh.diagnostico(cuentaCliente, "",
						"Canales habilitados en CM.");
		 }
		 
	}
	
}
