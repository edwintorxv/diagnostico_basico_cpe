package co.com.claro.ms_diagnostico_basico_cpe.application.service.utils;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.RequestOntDataDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseOntDataDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class HelperOnt {

    private final IPollerPortOut iPollerPortOut;

    public ResponseOntDataDto ontData(String macAddress) throws Exception {

        RequestOntDataDto requestOntDataDto = new RequestOntDataDto();
        requestOntDataDto.setMac(macAddress);
        return Optional.of(iPollerPortOut.obtenerOntData(requestOntDataDto))
                .orElse(null);
    }

    public boolean upTime(ResponseOntDataDto responseOntDataDto) {

        if (responseOntDataDto == null ||
                responseOntDataDto.getData() == null ||
                responseOntDataDto.getData().getUptime() == null ||
                responseOntDataDto.getData().getUptime().isBlank()) {
            return false;
        }

        long totalMillis = convertirUptimeAMillis(
                responseOntDataDto.getData().getUptime()
        );

        return totalMillis > 30 * 60 * 1000L;
    }

    private long convertirUptimeAMillis(String uptime) {
        if (uptime == null || uptime.isBlank()) {
            return 0;
        }

        long dias = 0;
        String tiempo;

        try {
            // Ej: "1 días, 03:57:44"
            if (uptime.contains("día")) {
                String[] partes = uptime.split(",");
                dias = Long.parseLong(
                        partes[0].replaceAll("[^0-9]", "")
                );
                tiempo = partes[1].trim();
            } else {
                // Ej: "03:57:44"
                tiempo = uptime.trim();
            }

            String[] hms = tiempo.split(":");
            long horas = Long.parseLong(hms[0]);
            long minutos = Long.parseLong(hms[1]);
            long segundos = Long.parseLong(hms[2]);

            return
                    dias * 24 * 60 * 60 * 1000 +
                            horas * 60 * 60 * 1000 +
                            minutos * 60 * 1000 +
                            segundos * 1000;

        } catch (Exception e) {
            // Ideal: loggear
            System.err.println("Error parseando uptime: " + uptime);
            return 0;
        }
    }

}
