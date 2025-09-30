package co.com.claro.ms_diagnostico_basico_cpe.application.service.utils;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoFtthLineaBaseDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.ResponseDiagnosticoFtthLineaBaseDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.*;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class HelperLineaBaseFtth {

    private final IPollerPortOut iPollerPortOut;


    public ResponseDiagnosticoFtthLineaBaseDto respuestaGenerica(String codigo, String descripcion, String cuentaCliente,
                                                                 String estadoOnt, String vecinos) {

        ResponseDiagnosticoFtthLineaBaseDto response = new ResponseDiagnosticoFtthLineaBaseDto();

        response.setStatus("Ok");
        response.setMessage(ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY);
        response.setData(
                new DiagnosticoFtthLineaBaseDto(
                        codigo,
                        descripcion,
                        cuentaCliente,
                        estadoOnt,
                        vecinos
                )
        );
        return response;
    }

    public ResponseOntDataDto ontData(String macAddress) throws Exception {

        RequestOntDataDto requestOntDataDto = new RequestOntDataDto();
        requestOntDataDto.setMac(macAddress);
        ResponseOntDataDto responseOntDataDto = iPollerPortOut.obtenerOntData(requestOntDataDto);

        if (responseOntDataDto != null) {
            return responseOntDataDto;
        } else {
            return null;
        }
    }

    public List<NeighborStatusFtthDto> vecinos(ResponseOntDataDto responseOntDataDto) throws Exception {

        RequestNeighborStatusFtthDto requestNeighborStatusFtthDto = new RequestNeighborStatusFtthDto();
        requestNeighborStatusFtthDto.setOntDataDto(responseOntDataDto.getData());
        requestNeighborStatusFtthDto.setSamples(30);
        requestNeighborStatusFtthDto.setGeodistanceQuery(false);

        ResponseNeighborStatusFtthDto responseNeighborStatusFtthDto = iPollerPortOut.obtenerVecinosFtth(requestNeighborStatusFtthDto);

        if (!responseNeighborStatusFtthDto.getData().isEmpty()) {
            return responseNeighborStatusFtthDto.getData();
        } else {
            return null;
        }
    }

    public boolean masividadVecinos(List<NeighborStatusFtthDto> lstNeighborStatusFtthDto) {

        int cantidadVecinosCaidos = 0;

        for (NeighborStatusFtthDto neighborStatusFtthDto : lstNeighborStatusFtthDto) {
            if ("false".equalsIgnoreCase(neighborStatusFtthDto.getAlive())) {
                cantidadVecinosCaidos++;
            }
        }
        return (cantidadVecinosCaidos * 100.0) / lstNeighborStatusFtthDto.size() >= 80.0;
    }

    public boolean upTime(ResponseOntDataDto responseOntDataDto) {

        long totalMillis = convertirUptimeAMillis(responseOntDataDto.getData().getUptime());
        long treintaMinutosMillis = 30 * 60 * 1000L; // 30 min en ms
        return totalMillis > treintaMinutosMillis;

    }

    private long convertirUptimeAMillis(String upTime) {
        long milisegundos = 0;

        try {
            String[] partes = upTime.split(" ");
            int dias = 0;
            String tiempo;

            if (partes.length == 3) {
                String parteDias = partes[1].trim();
                if (parteDias.contains("día")) {
                    dias = Integer.parseInt(partes[0]);
                }
                tiempo = partes[2].trim();
            } else {
                tiempo = upTime.trim();
            }

            String[] hms = tiempo.split(":");
            int horas = Integer.parseInt(hms[0]);
            int minutos = Integer.parseInt(hms[1]);

            double segundos = Double.parseDouble(hms[2]);

            milisegundos += dias * 24L * 60L * 60L * 1000L;
            milisegundos += horas * 60L * 60L * 1000L;
            milisegundos += minutos * 60L * 1000L;
            milisegundos += (long) (segundos * 1000);


        } catch (Exception e) {
            System.err.println("Error al parsear uptime: " + upTime);
        }

        return milisegundos;
    }

    public ResponseRealTimeMeasurementFtthDto realtimeMeasurement(ResponseOntDataDto ontDataDto) throws Exception {

        RequestRealTimeMeasurementFtthDto requestRealTimeMeasurementFtthDto = new RequestRealTimeMeasurementFtthDto();
        requestRealTimeMeasurementFtthDto.setOntDataDto(ontDataDto.getData());
        ResponseRealTimeMeasurementFtthDto responseRealTimeMeasurementFtthDto = iPollerPortOut.obtenerNivelesComunicacion(requestRealTimeMeasurementFtthDto);
        if (responseRealTimeMeasurementFtthDto.getData() != null) {
            return responseRealTimeMeasurementFtthDto;
        } else {
            return null;
        }
    }

    public boolean validacioNivelesOnt(RealTimeMeasurementFtthDto realTimeMeasurementFtthDto) {

        double rx = Double.parseDouble(realTimeMeasurementFtthDto.getRx());
        double tx = Double.parseDouble(realTimeMeasurementFtthDto.getTx());

        if (validacionRangosOnt(rx, tx)) {
            return true;
        }
        return false;
    }

    public boolean validacionNivelesOntVecinos(List<NeighborStatusFtthDto> lstNeighborStatusFtthDtos) {

        int vecinoFueraDeNivel = 0;

        for (NeighborStatusFtthDto neighborStatusFtthDto : lstNeighborStatusFtthDtos) {
            double rx = neighborStatusFtthDto.getRx();
            double tx = Double.parseDouble(neighborStatusFtthDto.getTx());

            if (validacionRangosOnt(rx, tx)) {
                vecinoFueraDeNivel++;
            }
        }

        return (vecinoFueraDeNivel * 100.0) / lstNeighborStatusFtthDtos.size() >= 80.0;
    }


    public boolean validacionRangosOnt(double tx, double rx) {

        boolean rxFueraRango = (rx < 0 || rx > 5);
        boolean txFueraRango = (tx < -27 || tx > -8);

        return rxFueraRango || txFueraRango;
    }


}
