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
