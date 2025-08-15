package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest.configuration;

import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.configuration.ISwaggerRepositoryPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 12/02/2025
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/swagger")
public class SwaggerController {

    private final ISwaggerRepositoryPortIn swaggerRepositoryPortIn;

    // Endpoint para obtener los logs
    @Hidden
    @GetMapping
    public ResponseEntity<String> getSwagger(
            @Parameter(hidden = true) Transaction transaction) throws Exception {

        String response = swaggerRepositoryPortIn.getSwagger(transaction);
        return ResponseEntity.ok(response);

    }

}
