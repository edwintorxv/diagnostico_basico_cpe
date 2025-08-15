package co.com.claro.ms_diagnostico_basico_cpe;

import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.ParametersConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MsDiagnosticoBasicoCpeApplication {

    public static void main(String[] args) {
        ParametersConfig.init(SpringApplication.run(MsDiagnosticoBasicoCpeApplication.class, args)
                .getEnvironment().getProperty("spring.application.name", "default-app"));
    }

}
