package co.com.claro.ms_diagnostico_basico_cpe.application.service.utils;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.PingCpeService;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.TraceCpeService;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.*;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.*;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.*;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.ParametersConfig;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.configuration.Transaction;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@RequiredArgsConstructor
@Component
public class HelperDiagnosticoConectividadFtth {

    Transaction transaction = Transaction.startTransaction();
    private final PingCpeService pingCpeService;
    private final TraceCpeService traceCpeService;
    private final IPollerPortOut iPollerPortOut;

    public ResponseDiagnosticoConectividadFtthDto respuestaValdacionesFuncionales(String cuentaCliente, String codigoDiagnostico,
                                                                                  String descripcionDiagnostico, String estadoFinal) {

        ResponseDiagnosticoConectividadFtthDto response = new ResponseDiagnosticoConectividadFtthDto();

        response.setStatus("Ok");
        response.setMessage(ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY);
        DiagnosticoConectividadFtthDto diagnostico = DiagnosticoConectividadFtthDto.builder()
                .idDiagnostico(codigoDiagnostico)
                .descripcion(descripcionDiagnostico)
                .timeStamp(formatearFecha(new Date()))
                .cuentaCliente(cuentaCliente)

                .macAddress("")
                .marca("")
                .modelo("")
                .firmware("")
                .ipWAN("")
                .uptime("")
                .requiereReinicio(false)
                .capacidadDiagnostico(false)
                .estadoFinal(estadoFinal)
                .pruebasConectividad(null)
                .analisis(null)
                .accionesSugeridas(List.of(
                        construirAccionSugerida(
                                "",
                                "",
                                ""
                        )
                ))
                .build();

        response.setData(diagnostico);

        return response;
    }


    public ResponseDiagnosticoConectividadFtthDto respuestaGenericaErrores(String cuentaCliente,
                                                                           ResponseOntDataDto responseOntDataDto,
                                                                           String idDiagnostico) throws Exception {

        ResponseDiagnosticoConectividadFtthDto response = new ResponseDiagnosticoConectividadFtthDto();
        ResponseIpOntDto responseIpOntDto = responseIpOntDto(responseOntDataDto);

        DiagnosticoResultadoDto resultadoDto = crearDiagnosticoErrores(idDiagnostico);

        String ipGestion = null;
        if (responseIpOntDto != null &&
                responseIpOntDto.getData() != null &&
                responseIpOntDto.getData().getGestion() != null) {
            ipGestion = responseIpOntDto.getData().getGestion();
        }

        response.setStatus("Ok");
        response.setMessage(ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY);
        DiagnosticoConectividadFtthDto diagnostico = DiagnosticoConectividadFtthDto.builder()
                .idDiagnostico(resultadoDto.getIdDiagnostico())
                .descripcion(resultadoDto.getDescripcion())
                .timeStamp(formatearFecha(new Date()))
                .cuentaCliente(cuentaCliente)
                .macAddress(responseOntDataDto.getData().getMac())
                .marca(responseOntDataDto.getData().getVendor())
                .modelo(responseOntDataDto.getData().getModel())
                .firmware(responseOntDataDto.getData().getFirmware())
                .ipWAN(ipGestion)
                .uptime(responseOntDataDto.getData().getUptime())
                .requiereReinicio(resultadoDto.isRequiereReinicio())
                .capacidadDiagnostico(resultadoDto.isCapacidadDiagnostico())
                .estadoFinal(resultadoDto.getEstadoFinal())
                .pruebasConectividad(null)
                .analisis(resultadoDto.getAnalisisDto())
                .accionesSugeridas(resultadoDto.getAccionSugeridaDtoList())
                .build();

        response.setData(diagnostico);

        return response;
    }

    public ResponseDiagnosticoConectividadFtthDto respuestaGenericaPruebasConexion(String cuentaCliente,
                                                                                   ResponseOntDataDto responseOntDataDto,
                                                                                   String idDiagnostico) throws Exception {

        ResponseDiagnosticoConectividadFtthDto response = new ResponseDiagnosticoConectividadFtthDto();
        ResponseIpOntDto responseIpOntDto = responseIpOntDto(responseOntDataDto);

        DiagnosticoResultadoDto resultadoDto = crearDiagnosticoConectividad(idDiagnostico);

        CompatibilidadPruebasDto compatibilidad = compatibilidadPruebasDto(responseOntDataDto.getData().getModel());

        String ipGestion = null;
        if (responseIpOntDto != null &&
                responseIpOntDto.getData() != null &&
                responseIpOntDto.getData().getGestion() != null) {
            ipGestion = responseIpOntDto.getData().getGestion();
        }

        response.setStatus("Ok");
        response.setMessage(ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY);
        DiagnosticoConectividadFtthDto diagnostico = DiagnosticoConectividadFtthDto.builder()
                .idDiagnostico(resultadoDto.getIdDiagnostico())
                .descripcion(resultadoDto.getDescripcion())
                .timeStamp(formatearFecha(new Date()))
                .cuentaCliente(cuentaCliente)
                .macAddress(responseOntDataDto.getData().getMac())
                .marca(responseOntDataDto.getData().getVendor())
                .modelo(responseOntDataDto.getData().getModel())
                .firmware(responseOntDataDto.getData().getFirmware())
                .ipWAN(ipGestion)
                .uptime(responseOntDataDto.getData().getUptime())
                .requiereReinicio(resultadoDto.isRequiereReinicio())
                .capacidadDiagnostico(resultadoDto.isCapacidadDiagnostico())
                .estadoFinal(resultadoDto.getEstadoFinal())
                //Corregir acá porque ya no se ejecuta fqdn
                .pruebasConectividad(pruebasConectividad(responseOntDataDto, compatibilidad, idDiagnostico))
                .analisis(resultadoDto.getAnalisisDto())
                .accionesSugeridas(resultadoDto.getAccionSugeridaDtoList())
                .build();

        response.setData(diagnostico);

        return response;
    }

    //Independizar FQDN
    public ResponseDiagnosticoConectividadFtthDto respuestaPruebasConexionFqdn(ResponseOntDataDto responseOntDataDto,
                                                                               String cuentaCliente, String ipWan) throws Exception {

        ResponseDiagnosticoConectividadFtthDto response = new ResponseDiagnosticoConectividadFtthDto();

        DiagnosticoResultadoDto resultadoDto = new DiagnosticoResultadoDto();

        PruebasConectividadDto pruebasConectivaidad = new PruebasConectividadDto();

        //Prueba FQDN
        PingCpeRequestDto pingFqdnCpeRequestDto = getPingCpeRequestDto(responseOntDataDto, "true");
        PingCpeResponseDto pingFqdnCpeResponseDto = respuestaPing(pingFqdnCpeRequestDto);

        int success = Integer.parseInt(pingFqdnCpeResponseDto.getPingInfo().getSuccessCount());

        if (success == 0) {
            return respuestaGenericaErrores(cuentaCliente, responseOntDataDto, "503");
        } else {
            resultadoDto = crearDiagnosticoConectividad("500");
        }

        //Prueba Ping normal
        PingCpeRequestDto pingNormalCpeRequestDto = getPingCpeRequestDto(responseOntDataDto, "false");
        PingCpeResponseDto pingNormalCpeResponseDto = respuestaPing(pingNormalCpeRequestDto);

        //Prueba Trace
        TraceCpeRequestDto traceCpeRequestDto = getTraceCpeRequestDto(responseOntDataDto);
        TraceCpeResponseDto traceCpeResponseDto = respuestaTrace(traceCpeRequestDto);

        pruebasConectivaidad.setPingFQDN(pingFqdnCpeResponseDto.getPingInfo());
        pruebasConectivaidad.setPing(pingNormalCpeResponseDto);
        pruebasConectivaidad.setTraceCpeResponseDto(traceCpeResponseDto);

        response.setStatus("Ok");
        response.setMessage(ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY);
        DiagnosticoConectividadFtthDto diagnostico = DiagnosticoConectividadFtthDto.builder()
                .idDiagnostico(resultadoDto.getIdDiagnostico())
                .descripcion(resultadoDto.getDescripcion())
                .timeStamp(formatearFecha(new Date()))
                .cuentaCliente(cuentaCliente)
                .macAddress(responseOntDataDto.getData().getMac())
                .marca(responseOntDataDto.getData().getVendor())
                .modelo(responseOntDataDto.getData().getModel())
                .firmware(responseOntDataDto.getData().getFirmware())
                .ipWAN(ipWan)
                .uptime(responseOntDataDto.getData().getUptime())
                .requiereReinicio(resultadoDto.isRequiereReinicio())
                .capacidadDiagnostico(resultadoDto.isCapacidadDiagnostico())
                .estadoFinal(resultadoDto.getEstadoFinal())
                .pruebasConectividad(pruebasConectivaidad)
                .analisis(resultadoDto.getAnalisisDto())
                .accionesSugeridas(resultadoDto.getAccionSugeridaDtoList())
                .build();

        response.setData(diagnostico);

        return response;
    }

    public String paquetesLatenciaPing(ResponseOntDataDto responseOntDataDto) throws Exception {

        PingCpeRequestDto pingCpeRequestDto = getPingCpeRequestDto(responseOntDataDto, "false");

        PingCpeResponseDto pingCpeResponseDto = respuestaPing(pingCpeRequestDto);

        int paquetesCompletos = Integer.parseInt(pingCpeResponseDto.getPingInfo().getSuccessCount());
        int paquetesFallidos = Integer.parseInt(pingCpeResponseDto.getPingInfo().getFailureCount());
        int latenciaPromedio = Integer.parseInt(pingCpeResponseDto.getPingInfo().getAverageResponseTime());

        int total = paquetesCompletos + paquetesFallidos;
        if (total == 0) total = 1;

        double perdida = (paquetesFallidos * 100.0) / total;

        //Perdida 100% ejecutar traceroute
        if (perdida == 100) {

            //Prueba Trace
            TraceCpeRequestDto traceCpeRequestDto = new TraceCpeRequestDto();
            TraceCpeResponseDto traceCpeResponseDto = respuestaTrace(traceCpeRequestDto);

            List<TraceCpeHopDto> hops = traceCpeResponseDto.getTraceInfo().getHops();

            // Si no hay hops no hay ruta
            if (hops == null || hops.isEmpty()) {
                return "504";
            }

            // Si hay más de 1 hop ya hay progreso
            boolean trazaProgresa = hops.size() > 1;

            // Revisar si la traza llega al destino final
            TraceCpeHopDto ultimoHop = hops.get(hops.size() - 1);
            boolean llegaDestino = ultimoHop.getHost() != null
                    && ultimoHop.getHost().equals("8.8.8.8");

            if (llegaDestino) {
                // La traza llegó, se evalúa calidad igual que si pérdida < 100
                if (perdida == 0 && latenciaPromedio < 30) return "500";
                return "505";
            }

            if (trazaProgresa) {
                // evaluar calidad normal
                if (perdida == 0 && latenciaPromedio < 30) return "500";
                return "505";
            }

            // Solo un hop 1 y se corta problema en ONT/OLT
            return "504";
        }

        // Sin pérdida y buena latencia
        if (perdida == 0 && latenciaPromedio < 30) {
            return "500";
        }

        // Pérdida parcial o latencia alta
        if ((perdida > 0 && perdida < 100) || latenciaPromedio > 100) {
            return "505";
        }

        return "";
    }

    private static TraceCpeRequestDto getTraceCpeRequestDto(ResponseOntDataDto responseOntDataDto) {

        TraceCpeRequestDto traceReq = new TraceCpeRequestDto();
        traceReq.setOriginId("TyD");
        traceReq.setSerial(responseOntDataDto.getData().getSerial());
        traceReq.setAccount(responseOntDataDto.getData().getRrAccount());
        traceReq.setVendor(responseOntDataDto.getData().getVendor());
        traceReq.setModel(responseOntDataDto.getData().getModel());
        traceReq.setMaxHopCount("30");
        traceReq.setTimeout("5000");

        return traceReq;
    }

    public TraceCpeResponseDto respuestaTrace(TraceCpeRequestDto traceCpeRequestDto) throws Exception {

        TraceCpeResponseDto traceCpeResponseDto;
        traceCpeResponseDto = traceCpeService.dataTraceCpe(traceCpeRequestDto);

        return traceCpeResponseDto;
    }

    private static PingCpeRequestDto getPingCpeRequestDto(ResponseOntDataDto responseOntDataDto, String pruebaFqdn) {
        PingCpeRequestDto pingCpeRequestDto = new PingCpeRequestDto();
        pingCpeRequestDto.setOriginId("TyD");
        pingCpeRequestDto.setSerial(responseOntDataDto.getData().getSerial());
        pingCpeRequestDto.setAccount(responseOntDataDto.getData().getRrAccount());
        pingCpeRequestDto.setVendor(responseOntDataDto.getData().getVendor());
        pingCpeRequestDto.setModel(responseOntDataDto.getData().getModel());
        pingCpeRequestDto.setMaxHopCount("30");
        pingCpeRequestDto.setTimeout("5000");
        pingCpeRequestDto.setFqdn(pruebaFqdn);

        return pingCpeRequestDto;
    }

    public PingCpeResponseDto respuestaPing(PingCpeRequestDto pingCpeRequestDto) throws Exception {

        PingCpeResponseDto pingCpeResponseDto;
        pingCpeResponseDto = pingCpeService.dataPingCpe(pingCpeRequestDto);

        return pingCpeResponseDto;
    }


    private PruebasConectividadDto pruebasConectividad(ResponseOntDataDto responseOntDataDto, CompatibilidadPruebasDto compatibilidad,
                                                       String idDiagnostico) throws Exception {

        PruebasConectividadDto pruebasConectivaidad = new PruebasConectividadDto();

        String serial = responseOntDataDto.getData().getSerial();
        String account = responseOntDataDto.getData().getRrAccount();
        String vendor = responseOntDataDto.getData().getVendor();
        String model = responseOntDataDto.getData().getModel();
        String hop = "30";
        String timeout = "5000";

        PingCpeResponseDto pingIp = null;
        if (compatibilidad.isPingIp()) {
            PingCpeRequestDto reqPingIp = new PingCpeRequestDto();
            reqPingIp.setOriginId("TyD");
            reqPingIp.setSerial(serial);
            reqPingIp.setAccount(account);
            reqPingIp.setVendor(vendor);
            reqPingIp.setModel(model);
            reqPingIp.setMaxHopCount(hop);
            reqPingIp.setTimeout(timeout);

            pingIp = respuestaPing(reqPingIp);
            pruebasConectivaidad.setPing(pingIp);
        }

        if (compatibilidad.isTrazaIp() && pingIp != null) {

            int completo = Integer.parseInt(pingIp.getPingInfo().getSuccessCount());
            int falla = Integer.parseInt(pingIp.getPingInfo().getFailureCount());
            int total = (completo + falla == 0) ? 1 : (completo + falla);

            double perdida = (falla * 100.0) / total;

            if (perdida == 100 || "500".equalsIgnoreCase(idDiagnostico)) {
                TraceCpeRequestDto traceReq = new TraceCpeRequestDto();
                traceReq.setOriginId("TyD");
                traceReq.setSerial(serial);
                traceReq.setAccount(account);
                traceReq.setVendor(vendor);
                traceReq.setModel(model);
                traceReq.setMaxHopCount(hop);
                traceReq.setTimeout(timeout);

                TraceCpeResponseDto trace = respuestaTrace(traceReq);
                pruebasConectivaidad.setTraceCpeResponseDto(trace);
            }
        }

        return pruebasConectivaidad;
    }


    public ResponseIpOntDto responseIpOntDto(ResponseOntDataDto responseOntDataDto) throws Exception {

        RequestIpOntDto requestIpOntDto = new RequestIpOntDto();

        if (responseOntDataDto == null || responseOntDataDto.getData() == null) {
            return null;
        }

        requestIpOntDto.setOntDataDto(responseOntDataDto.getData());

        return iPollerPortOut.obetnerIpOnt(requestIpOntDto);

    }

    public CompatibilidadPruebasDto compatibilidadPruebasDto(String modeloOnt) {

        RequestModeloSoportePruebasDto requestModelo = new RequestModeloSoportePruebasDto();
        requestModelo.setModelo(modeloOnt);

        ResponseModeloSoportePruebasDto response =
                iPollerPortOut.recuperarCompatibilidadPruebas(requestModelo);

        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            return new CompatibilidadPruebasDto(false, false, false, false);
        }

        ModeloSoportePruebasDto modelo = response.getData().get(0);

        boolean pingIp = "SI".equalsIgnoreCase(modelo.getPingIp());
        boolean pingFqdn = "SI".equalsIgnoreCase(modelo.getPingFqdn());
        boolean trazaIp = "SI".equalsIgnoreCase(modelo.getTrazaIp());
        boolean trazaFqdn = "SI".equalsIgnoreCase(modelo.getTrazaFqdn());

        return new CompatibilidadPruebasDto(pingIp, pingFqdn, trazaIp, trazaFqdn);
    }

    public AnalisisDto construirAnalisis(String condicion, String conclusion) {
        return AnalisisDto.builder()
                .condicion(condicion)
                .conclusion(conclusion)
                .build();
    }

    public AccionSugeridaDto construirAccionSugerida(String tipo, String nivel, String motivo) {
        return AccionSugeridaDto.builder()
                .tipo(tipo)
                .nivel(nivel)
                .motivo(motivo)
                .build();
    }

    public String formatearFecha(Date fecha) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
        return sdf.format(fecha);
    }

    public boolean validacionIpWan(String ipWan) {

        if (ipWan == null || ipWan.isEmpty() || ipWan.isBlank()) {
            return false;
        } else {

            String segmento = ipWan.split("\\.")[0];
            return segmento.equals("10") || segmento.equals("100");
        }

    }

    private DiagnosticoResultadoDto crearDiagnosticoErrores(String idFlujo) throws Exception {

        DiagnosticoResultadoDto resultadoDto = new DiagnosticoResultadoDto();

        switch (idFlujo) {
            case "507":
                resultadoDto.setIdDiagnostico(
                        ParametersConfig.getPropertyValue(
                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_OFFLINE_CODIGO, transaction));
                resultadoDto.setDescripcion(
                        ParametersConfig.getPropertyValue(
                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_OFFLINE_DESCRIPCION, transaction));
                resultadoDto.setEstadoFinal(
                        ParametersConfig.getPropertyValue(
                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_OFFLINE_ESTADO_FINAL, transaction));
                resultadoDto.setRequiereReinicio(false);
                resultadoDto.setCapacidadDiagnostico(false);
                resultadoDto.setAnalisisDto(construirAnalisis(
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_OFFLINE_ANALISIS_CONDICION, transaction),
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_OFFLINE_ANALISIS_CONCLUSION, transaction)
                        )
                );
                resultadoDto.setAccionSugeridaDtoList(
                        List.of(
                                construirAccionSugerida(
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_OFFLINE_ACCIONES_TIPO, transaction),
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_OFFLINE_ACCIONES_NIVEL, transaction),
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_OFFLINE_ACCIONES_MOTIVO, transaction)
                                )
                        )
                );
                break;

            case "501":
                resultadoDto.setIdDiagnostico(
                        ParametersConfig.getPropertyValue(
                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_UP_TIME_MAYOR_CODIGO, transaction));
                resultadoDto.setDescripcion(
                        ParametersConfig.getPropertyValue(
                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_UP_TIME_MAYOR_DESCRIPCION, transaction));
                resultadoDto.setEstadoFinal(
                        ParametersConfig.getPropertyValue(
                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_UP_TIME_MAYOR_ESTADO_FINAL, transaction));
                resultadoDto.setRequiereReinicio(true);
                resultadoDto.setCapacidadDiagnostico(true);
                resultadoDto.setAnalisisDto(construirAnalisis(
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_UP_TIME_MAYOR_ANALISIS_CONDICION, transaction),
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_UP_TIME_MAYOR_ANALISIS_CONCLUCION, transaction)
                        )
                );
                resultadoDto.setAccionSugeridaDtoList(
                        List.of(
                                construirAccionSugerida(
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_UP_TIME_MAYOR_ACCIONES_TIPO, transaction),
                                        "",
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_UP_TIME_MAYOR_ACCIONES_ACCION, transaction)
                                )
                        )
                );

                break;

            case "502":
                resultadoDto.setIdDiagnostico(ParametersConfig.getPropertyValue(Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_ERROR_IP_CODIGO, transaction));
                resultadoDto.setDescripcion(ParametersConfig.getPropertyValue(Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_ERROR_IP_DESCRIPCION, transaction));
                resultadoDto.setEstadoFinal(ParametersConfig.getPropertyValue(Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_ERROR_IP_ESTADO_FINAL, transaction));
                resultadoDto.setRequiereReinicio(false);
                resultadoDto.setCapacidadDiagnostico(true);
                resultadoDto.setAnalisisDto(construirAnalisis(
                                ParametersConfig.getPropertyValue(Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_ERROR_IP_ANALISIS_CONDICION, transaction),
                                ParametersConfig.getPropertyValue(Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_ERROR_IP_ANALISIS_CONCLUCION, transaction)
                        )
                );
                resultadoDto.setAccionSugeridaDtoList(
                        List.of(
                                construirAccionSugerida(
                                        ParametersConfig.getPropertyValue(Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_ERROR_IP_ACCIONES_TIPO, transaction),
                                        ParametersConfig.getPropertyValue(Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_ERROR_IP_ACCIONES_NIVEL, transaction),
                                        ParametersConfig.getPropertyValue(Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_ERROR_IP_ACCIONES_MOTIVO, transaction)
                                )
                        )
                );

                break;

            case "503":
                resultadoDto.setIdDiagnostico(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_ENCUENTRA_HOST_CODIGO, transaction));
                resultadoDto.setDescripcion(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_ENCUENTRA_HOST_DESCRIPCION, transaction));
                resultadoDto.setEstadoFinal(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_ENCUENTRA_HOST_ESTADO_FINAL, transaction));
                resultadoDto.setRequiereReinicio(false);
                resultadoDto.setCapacidadDiagnostico(false);
                resultadoDto.setAnalisisDto(construirAnalisis(
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_ENCUENTRA_HOST_ANALISIS_CONDICION, transaction),
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_ENCUENTRA_HOST_ANALISIS_CONCLUCION, transaction)
                        )
                );
                resultadoDto.setAccionSugeridaDtoList(
                        List.of(
                                construirAccionSugerida(
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_ENCUENTRA_HOST_ACCIONES_TIPO, transaction),
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_ENCUENTRA_HOST_ACCIONES_NIVEL, transaction),
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_ENCUENTRA_HOST_ACCIONES_MOTIVO, transaction)
                                )
                        )
                );

                break;

            case "506":
                resultadoDto.setIdDiagnostico(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_SOPORTA_PRUEBA_PING_TRACEROUT_CODIGO, transaction));
                resultadoDto.setDescripcion(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_SOPORTA_PRUEBA_PING_TRACEROUT_DESCRIPCION, transaction));
                resultadoDto.setEstadoFinal(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_SOPORTA_PRUEBA_PING_TRACEROUT_ESTADO_FINAL, transaction));
                resultadoDto.setRequiereReinicio(false);
                resultadoDto.setCapacidadDiagnostico(false);
                resultadoDto.setAnalisisDto(construirAnalisis(
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_SOPORTA_PRUEBA_PING_TRACEROUT_ANALISIS_CONDICION, transaction),
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_SOPORTA_PRUEBA_PING_TRACEROUT_ANALISIS_CONCLUCION, transaction)
                        )
                );
                resultadoDto.setAccionSugeridaDtoList(
                        List.of(
                                construirAccionSugerida(
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_SOPORTA_PRUEBA_PING_TRACEROUT_ACCIONES_TIPO, transaction),
                                        "",
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_NO_SOPORTA_PRUEBA_PING_TRACEROUT_ACCIONES_MOTIVO, transaction)
                                )
                        )
                );

                break;
        }

        return resultadoDto;
    }

    private DiagnosticoResultadoDto crearDiagnosticoConectividad(String idFlujo) throws Exception {

        DiagnosticoResultadoDto resultadoDto = new DiagnosticoResultadoDto();

        switch (idFlujo) {
            case "500":
                resultadoDto.setIdDiagnostico(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_VALORES_RANGOS_PERMITIDOS_CODIGO, transaction));
                resultadoDto.setDescripcion(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_VALORES_RANGOS_PERMITIDOS_DESCRIPCION, transaction));
                resultadoDto.setEstadoFinal(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_VALORES_RANGOS_PERMITIDOS_ESTADO_FINAL, transaction));
                resultadoDto.setRequiereReinicio(false);
                resultadoDto.setCapacidadDiagnostico(true);
                resultadoDto.setAnalisisDto(construirAnalisis(
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_VALORES_RANGOS_PERMITIDOS_ANALISIS_CONDCION, transaction),
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_VALORES_RANGOS_PERMITIDOS_ANALISIS_CONCLUCION, transaction)
                        )
                );
                resultadoDto.setAccionSugeridaDtoList(Collections.emptyList());

                break;

            case "505":
                resultadoDto.setIdDiagnostico(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_PERDIDA_PAQUETES_CODIGO, transaction));
                resultadoDto.setDescripcion(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_PERDIDA_PAQUETES_DESCRIPCION, transaction));
                resultadoDto.setEstadoFinal(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_PERDIDA_PAQUETES_ESTADO_FINAL, transaction));
                resultadoDto.setRequiereReinicio(false);
                resultadoDto.setCapacidadDiagnostico(true);
                resultadoDto.setAnalisisDto(construirAnalisis(
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_PERDIDA_PAQUETES_ANALISIS_CONDICION, transaction),
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_PERDIDA_PAQUETES_ANALISIS_CONCLUCION, transaction)
                        )
                );
                resultadoDto.setAccionSugeridaDtoList(
                        List.of(
                                construirAccionSugerida(
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_PERDIDA_PAQUETES_ACCTIONES_TIPO, transaction),
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_PERDIDA_PAQUETES_ACCIONES_NIVLEL, transaction),
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_PERDIDA_PAQUETES_ACCIONES_MOTIVO, transaction)
                                )
                        )
                );
                break;

            case "504":
                resultadoDto.setIdDiagnostico(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_FALLA_ROUTING_INTERNET_CODIGO, transaction));
                resultadoDto.setDescripcion(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_FALLA_ROUTING_INTERNET_DESCRIPCION, transaction));
                resultadoDto.setEstadoFinal(ParametersConfig.getPropertyValue(
                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_FALLA_ROUTING_INTERNET_ESTADO_FINAL, transaction));
                resultadoDto.setRequiereReinicio(false);
                resultadoDto.setCapacidadDiagnostico(true);
                resultadoDto.setAnalisisDto(construirAnalisis(
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_FALLA_ROUTING_INTERNET_ANALISIS_CONDICION, transaction),
                                ParametersConfig.getPropertyValue(
                                        Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_FALLA_ROUTING_INTERNET_ANALISIS_CONCLUCION, transaction)
                        )
                );
                resultadoDto.setAccionSugeridaDtoList(
                        List.of(
                                construirAccionSugerida(
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_FALLA_ROUTING_INTERNET_ACCIONES_TIPO, transaction),
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_FALLA_ROUTING_INTERNET_ACCIONES_NIVEL, transaction),
                                        ParametersConfig.getPropertyValue(
                                                Constantes.FTTH_DIAGNOSTICO_CONECTIVIDAD_ONT_FALLA_ROUTING_INTERNET_ACCIONES_MOTIVO, transaction)
                                )
                        )
                );
                break;
        }

        return resultadoDto;
    }

}
