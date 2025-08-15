package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.ConsultarEstadoDevice;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.ConsultarParametrosDipositivos;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceStatusResponse;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(Constantes.REQUEST_MAPPING)
@RequiredArgsConstructor
public class AcsController {

    private final ConsultarEstadoDevice consultarEstadoDevice;
    private final ConsultarParametrosDipositivos consultarParametrosDipositivos;

    @GetMapping("/deviceStatus/{serial}")
    public ResponseEntity<DeviceStatusResponse> obetenerEstado(@PathVariable String serial) throws Exception {
        return ResponseEntity.ok(consultarEstadoDevice.validarEstado(serial));
    }


    @PostMapping("/deviceParams")
    public ResponseEntity<DeviceParamsResponse> obtenerParametros(@RequestBody DeviceParamsDto deviceParamsDto) throws Exception {
        return ResponseEntity.ok(consultarParametrosDipositivos.consultarParametrosDispositivo(deviceParamsDto));
    }

}
