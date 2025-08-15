package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.configuration;

import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.configuration.ISwaggerRepositoryPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 12/02/2025
 */


@Primary
@Service
@RequiredArgsConstructor
public class SwaggerServiceImpl implements ISwaggerRepositoryPortIn {
    @Value("${server.port}")
    private String port;

    @Value("${springdoc.swagger-ui.path}")
    private String swagger;


    public String getSwagger(Transaction transaction) throws Exception {
        // Construir la URL de Swagger con la IP o dominio configurado
        return port + swagger;
    }
}

