package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario;

import java.util.List;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.ParametersConfig;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import org.springframework.stereotype.Service;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.utils.HelperMesh;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseGetWifiData;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;

@Service
public class TopologiaHfcSinMeshStrategy implements DiagnosticoTopologiaHfcStrategy {

    Transaction transaction = Transaction.startTransaction();


    //diagnosticar
    @Override
    public DiagnosticoResponse diagnosticar(InventarioPorTopoligiaDto inventario, IPollerPortOut pollerPortOut, IAcsPortOut acsPortOut) throws Exception {

        String cuentaCliente = inventario.getCuentaCliente();

        String formatearMac = HelperMesh.formatMacAddress(inventario.getInventarioCPE().getSerialMac());

        List<ResponseGetWifiData> getWifiData = pollerPortOut.consultarCMBandas(formatearMac);

        if (getWifiData == null || getWifiData.isEmpty()) {
        	return HelperMesh.diagnostico(
                    cuentaCliente,
                    ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_SIN_ULTRAWIFI_CANALES_DESHABILITADOS_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_SIN_ULTRAWIFI_CANALES_DESHABILITADOS_MENSAJE, transaction));

        } 
        
        boolean bandas = getWifiData.stream()
        .allMatch(banda -> "true".equalsIgnoreCase(banda.getEnableWireless()));
        
        if (bandas) {
        	
        	return HelperMesh.diagnostico(
                    cuentaCliente,
                    ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_SIN_ULTRAWIFI_TOPOLOGIA_CORRECTA_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_SIN_ULTRAWIFI_TOPOLOGIA_CORRECTA_MENSAJE, transaction));
            
        } else {
        	
        	return HelperMesh.diagnostico(
                    cuentaCliente,
                    ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_SIN_ULTRAWIFI_CANALES_DESHABILITADOS_CODIGO, transaction),
                    ParametersConfig.getPropertyValue(Constantes.HFC_ONLINE_SIN_ULTRAWIFI_CANALES_DESHABILITADOS_MENSAJE, transaction));

            
        }

    }
}
