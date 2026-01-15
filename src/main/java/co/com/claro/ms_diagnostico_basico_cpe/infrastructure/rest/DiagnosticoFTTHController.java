package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest;

import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.ResponseDiagnosticoConectividadFtthDto;
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
public class DiagnosticoFTTHController {

    private final IDiagnosticoFTTHPortIn iDiagnosticoFtthPortIn;

    @GetMapping("/diagnosticoConectividadFtth/{cuentaCliente}")
    public ResponseDiagnosticoConectividadFtthDto diagnosticoConectividadFtth(@PathVariable String cuentaCliente) throws Exception {
        return iDiagnosticoFtthPortIn.diagnosticoConectividad(cuentaCliente);
    }

}
