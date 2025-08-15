package co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.configuration;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 12/02/2025
 */
public interface ISwaggerRepositoryPortIn {
    String getSwagger(Transaction transaction) throws Exception;
}
