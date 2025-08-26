package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteRequest;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseArpPollerDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.poller.IPollerPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteConUltraWifiHfcService implements IPollerPortIn {
	
	 private final IPollerPortOut pollerPortOut;

	 @Override
	 public String consultarAPMaestro(String cuenta) throws Exception {

	     InventarioPorClienteRequest inventarioRequest = new InventarioPorClienteRequest();
	     inventarioRequest.setCuenta(cuenta);

	     InventarioPorClienteResponse inventarioPorClienteResponse =
	             pollerPortOut.obtenerInventarioPorCliente(inventarioRequest);

	     if (inventarioPorClienteResponse == null || inventarioPorClienteResponse.getData() == null) {
	         return "304";
	     }

	     List<InventarioPorClienteDto> listaInventarioInicial = inventarioPorClienteResponse.getData();

	     Optional<String> serialMacHfcOpt = listaInventarioInicial.stream()
	             .filter(item -> "HFC".equalsIgnoreCase(item.getProducto()))
	             .map(InventarioPorClienteDto::getSerialMac)
	             .findFirst();

	     if (serialMacHfcOpt.isEmpty()) {
	         return "304";
	     }

	     String serialMacSinFormato = serialMacHfcOpt.get();
	     String serialMacFormateada = formatMacAddress(serialMacSinFormato);
	     List<ResponseArpPollerDto> listaArp = pollerPortOut.consultarARP(serialMacFormateada);
	     
	     if (listaArp == null || listaArp.isEmpty()) {
	         return "304";
	     }

	   
	     Set<String> macsValidasInventario = listaInventarioInicial.stream()
	    	        .filter(item -> item.getMarca() != null && !item.getMarca().isEmpty() &&
	    	                         item.getModelo() != null && !item.getModelo().isEmpty())
	    	        // Asegúrate de que aquí también se use toUpperCase()
	    	        .map(item -> item.getSerialNumber().toUpperCase()) 
	    	        .collect(Collectors.toSet());


	     long numeroDeCoincidencias = listaArp.stream().filter(arpItem -> {
	         boolean estadoActivo = "Activo".equalsIgnoreCase(arpItem.getStatus());
	         String macArpSinFormato = arpItem.getMac().replace(":", "").toUpperCase();
	         boolean macCoincide = macsValidasInventario.contains(macArpSinFormato);
	         return estadoActivo && macCoincide;
	     }).count();


	     if (numeroDeCoincidencias == 1) {
	         return "True";
	     } else if (numeroDeCoincidencias >= 2) {
	         return "303";
	     } else { // Esto cubre el caso de 0 coincidencias
	         return "304";
	     }
	 }

	 /**
	  * Método de ayuda para formatear una MAC Address.
	  * Convierte "FC777B76F9F0" a "FC:77:7B:76:F9:F0".
	  * @param mac la MAC sin formato.
	  * @return la MAC con dos puntos.
	  */
	 private String formatMacAddress(String mac) {
	     if (mac == null || mac.length() != 12) {
	         // Devuelve el original o lanza una excepción si el formato es inválido
	         return mac; 
	     }
	     // Usa una expresión regular para insertar ":" cada dos caracteres.
	     return mac.replaceAll("(.{2})", "$1:").substring(0, 17);
	 }

}
