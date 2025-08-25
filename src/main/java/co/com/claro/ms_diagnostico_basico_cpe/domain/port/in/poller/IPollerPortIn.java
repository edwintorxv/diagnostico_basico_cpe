package co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.poller;


public interface IPollerPortIn {
	
	String consultarAPMaestro(String cuenta) throws Exception;
}
