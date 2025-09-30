package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.ResponseDiagnosticoFtthLineaBaseDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.in.diagnostico.IDiagnosticoFTTHPortIn;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
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
public class LineaBaseFTTHController {

    private final IDiagnosticoFTTHPortIn iDiagnosticoFtthPortIn;

    @GetMapping("/lineaBaseFtth/{cuentaCliente}")
    public ResponseDiagnosticoFtthLineaBaseDto diagnosticoLineaBaseHfc(@PathVariable String cuentaCliente) throws Exception {
        return iDiagnosticoFtthPortIn.diagnosticoLineaBase(cuentaCliente);
    }
}
