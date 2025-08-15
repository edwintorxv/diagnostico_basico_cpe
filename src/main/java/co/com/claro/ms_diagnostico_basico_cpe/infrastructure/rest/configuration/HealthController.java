package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest.configuration;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 21/11/2024
 */
@RestController
@RequestMapping("/health")
public class HealthController {
    @Hidden
    @GetMapping
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }

}