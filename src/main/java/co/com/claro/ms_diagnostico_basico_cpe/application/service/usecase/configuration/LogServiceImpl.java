package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.configuration;

import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.configuration.ILogRepositoryPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 21/11/2024
 */
@Primary
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements ILogRepositoryPortIn {
    @Value("${spring.logging.path}")
    private String logPath;

    @Value("${spring.application.name}")
    private String applicationName;

    // Método que lee el archivo de log y retorna su contenido
    public String getLogs(Transaction transaction) throws Exception {
        // Construye la ruta del archivo de log
        Path path = Paths.get(logPath, applicationName, applicationName + ".log");
        // Lee todas las líneas del archivo y las concatena en un solo String
        return Files.readString(path);
    }

}

