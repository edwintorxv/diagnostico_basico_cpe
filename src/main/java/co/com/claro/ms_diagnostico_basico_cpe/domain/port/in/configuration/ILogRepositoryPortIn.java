package co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.configuration;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 21/11/2024
 */
public interface ILogRepositoryPortIn {
    String getLogs(Transaction transaction) throws Exception;
}
