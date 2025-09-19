package co.com.claro.ms_diagnostico_basico_cpe.application.service.utils;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoHfcLineaBaseDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoHfcLineaBaseResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.*;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class HelperLineaBaseHfc {

    private final IPollerPortOut pollerPortOut;


    public ResponseCmDataPollerDto responseCmDataPollerDto(String macAddress) throws Exception {
        List<ResponseCmDataPollerDto> getCmData = pollerPortOut.consultarCMData(macAddress);
        if (!getCmData.isEmpty()) {
            ResponseCmDataPollerDto dataCableModem;
            dataCableModem = getCmData.get(0);
            return dataCableModem;

        } else {
            return null;
        }

    }

    public List<NeighborStatusDto> obtenerListadoVecinos(ResponseCmDataPollerDto dataCableModem) throws Exception {

        RequestNeighborStatusDto neighborStatusDto = new RequestNeighborStatusDto();
        neighborStatusDto.setCableModemDto(dataCableModem);
        neighborStatusDto.setSamples(30);
        neighborStatusDto.setGeodistanceQuery(false);
        ResponseNeighborStatusDto responseNeighborStatusDto = pollerPortOut.obtenerVecinos(neighborStatusDto);

        if (!responseNeighborStatusDto.getLstNeighbors().isEmpty()) {
            return responseNeighborStatusDto.getLstNeighbors();
        } else {
            return null;
        }
    }

    public List<ProvisioningDataDto> aprovisionamientoCliente(ResponseCmDataPollerDto dataCableModem) throws Exception {
        RequestProvisioningDataDto requestProvisioningDataDto = new RequestProvisioningDataDto();
        requestProvisioningDataDto.setCableModemDto(dataCableModem);
        ResponseProvisioningDataDto responseProvisioningDataDto = pollerPortOut.obtenerAprovisionamiento(requestProvisioningDataDto);

        if (!responseProvisioningDataDto.getLstProvisioningDataDto().isEmpty()) {
            return responseProvisioningDataDto.getLstProvisioningDataDto();
        } else {
            return null;
        }
    }

    public List<RealTimeMeasurementDto> realTimeMeasurementDto(ResponseCmDataPollerDto dataCableModem) {

        RequestRealTimeMeasurementDto requestRealTimeMeasurementDto = new RequestRealTimeMeasurementDto();
        requestRealTimeMeasurementDto.setCableModemDto(dataCableModem);
        ResponseRealTimeMeasurementDto realTimeMeasurementDto = pollerPortOut.obetnerNiveles(requestRealTimeMeasurementDto);

        if (!realTimeMeasurementDto.getLstRealTimeRealTimeMeasurement().isEmpty()) {
            return realTimeMeasurementDto.getLstRealTimeRealTimeMeasurement();
        } else {
            return null;
        }

    }

    public List<CableModemDataDto> datosCableModemPorModelo(ResponseCmDataPollerDto dataCableModem) throws Exception {
        RequestCableModemDataDto requestCableModemDataDto = new RequestCableModemDataDto();
        requestCableModemDataDto.setModelo(dataCableModem.getModel());
        ResponseCableModemDataDto responseCableModemDataDto = pollerPortOut.obtenerDataCableModem(requestCableModemDataDto);

        if (!responseCableModemDataDto.getLstCableModemDto().isEmpty()) {
            return responseCableModemDataDto.getLstCableModemDto();
        } else {
            return null;
        }

    }

    public double validacionRangoDeCaida(List<NeighborStatusDto> lstVecinos, ResponseCmDataPollerDto dataCableModem) {

        if (lstVecinos == null || lstVecinos.isEmpty()) {
            return 0.0;
        }

        long lastSeenCM = Long.parseLong(dataCableModem.getLastSeen());
        long unaHora = 3600_000L;
        long rangoInferior = lastSeenCM - unaHora;
        long rangoSuperior = lastSeenCM + unaHora;

        int vecinosCaidos = 0;

        for (NeighborStatusDto vecino : lstVecinos) {
            try {
                long lastSeenVecino = Long.parseLong(vecino.getLastSeen());
                if ((lastSeenVecino >= rangoInferior && lastSeenVecino <= rangoSuperior) && "false".equalsIgnoreCase(vecino.getAlive())) {
                    vecinosCaidos++;
                }
            } catch (NumberFormatException e) {
                System.err.println("Formato inválido en lastSeen vecino: " + vecino.getLastSeen());
            }
        }
        return (vecinosCaidos * 100.0) / lstVecinos.size();
    }

    public boolean validacionUpTime(String upTime) {

        long totalMillis = convertirUptimeAMillis(upTime);
        long treintaMinutosMillis = 30 * 60 * 1000L; // 30 min en ms
        return totalMillis > treintaMinutosMillis;
    }

    private long convertirUptimeAMillis(String upTime) {
        long milisegundos = 0;

        try {
            String[] partes = upTime.split(",");
            int dias = 0;
            String tiempo;

            if (partes.length == 2) {
                String parteDias = partes[0].trim();
                if (parteDias.contains("day")) {
                    dias = Integer.parseInt(parteDias.split(" ")[0]);
                }
                tiempo = partes[1].trim();
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

    public boolean validacionNivelesCM(List<RealTimeMeasurementDto> realTimeMeasurementDto) {

        for (RealTimeMeasurementDto realTimeMeasurementCM : realTimeMeasurementDto) {

            double tx = Double.parseDouble(realTimeMeasurementCM.getTx());
            double rx = Double.parseDouble(realTimeMeasurementCM.getRx());
            double snrUp = Double.parseDouble(realTimeMeasurementCM.getSnrUp());
            double snrDown = Double.parseDouble(realTimeMeasurementCM.getSnrDw());

            if (validacionRangosCM(tx, rx, snrUp, snrDown)) {
                return true;
            }
        }
        return false;
    }

    public boolean validacionNivelesVecinos(List<NeighborStatusDto> lstVecinos) {

        int vecinoFueradeNivel = 0;

        for (NeighborStatusDto retalTimeMeasurementsVecinos : lstVecinos) {

            double tx = Double.parseDouble(retalTimeMeasurementsVecinos.getTx());
            double rx = Double.parseDouble(retalTimeMeasurementsVecinos.getRx());
            double snrUp = Double.parseDouble(retalTimeMeasurementsVecinos.getSnrUp());
            double snrDown = Double.parseDouble(retalTimeMeasurementsVecinos.getSnrDw());

            if (validacionRangosCM(tx, rx, snrUp, snrDown)) {
                vecinoFueradeNivel++;
            }
        }

        return (vecinoFueradeNivel * 100.0) / lstVecinos.size() >= 80.0;
    }

    public boolean validacionRangosCM(double tx, double rx, double snrUp, double snrDown) {

        boolean txFueraRango = (tx < 3.6 || tx > 52.1);
        boolean rxFueraRango = (rx < -11.1 || rx > 11.1);
        boolean snrUpFueraRango = (snrUp < 29.9);
        boolean snrDownFueraRango = (snrDown < 32.9);

        return txFueraRango || rxFueraRango || snrUpFueraRango || snrDownFueraRango;

    }

    public boolean validacionDesposicionamiento(List<ProvisioningDataDto> lstProvisioningData, List<CableModemDataDto> lstCableModemDatDto) {

        if (lstProvisioningData == null || lstProvisioningData.isEmpty() || lstCableModemDatDto == null || lstCableModemDatDto.isEmpty()) {
            return false;
        }

        try {
            double aprovisionado = Double.parseDouble(lstProvisioningData.get(0).getIwDownstream());
            double capacidadCM = Double.parseDouble(lstCableModemDatDto.get(0).getVelocidadMaxima());

            return aprovisionado > capacidadCM;
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear valores de aprovisionamiento/capacidad");
            return false;
        }
    }

    public DiagnosticoHfcLineaBaseResponse respuestaGenerica(String codigo, String descripcion, String cuentaCliente,
                                                             String estadoCM, String vecinos) {
        DiagnosticoHfcLineaBaseResponse response = new DiagnosticoHfcLineaBaseResponse();
        response.setStatus("Ok");
        response.setMessage(ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY);
        response.setData(List.of(new DiagnosticoHfcLineaBaseDto(codigo, descripcion, cuentaCliente, estadoCM, vecinos)));
        return response;
    }


}
