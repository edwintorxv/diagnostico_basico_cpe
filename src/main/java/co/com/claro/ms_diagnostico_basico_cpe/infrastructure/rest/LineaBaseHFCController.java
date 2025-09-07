package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoHfcLineaBaseResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico.IDiagnosticoHFCPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.repository.adapter.DiagnosticoTopologiaHfcAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(Constantes.REQUEST_MAPPING)
@RequiredArgsConstructor
public class LineaBaseHFCController {

    private final DiagnosticoTopologiaHfcAdapter diagnosticoAdapter;
    private final IDiagnosticoHFCPortIn iDiagnosticoHFCPortIn;

    @GetMapping("/lineaBaseHFC/{cuentaCliente}")
    public DiagnosticoHfcLineaBaseResponse diagnosticoLineaBaseHfc(@PathVariable String cuentaCliente) throws Exception{
        return iDiagnosticoHFCPortIn.diagnosticoLineaBaseHfc(cuentaCliente);
    }

}
