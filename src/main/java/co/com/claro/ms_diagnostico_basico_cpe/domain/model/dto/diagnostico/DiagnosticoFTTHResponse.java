package co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico;

import lombok.Data;

@Data
public class DiagnosticoFTTHResponse {

    private String cuenta;
    private String codigo;
    private String descripcion;

    public DiagnosticoFTTHResponse(String cuentaCliente, String inventarioNoEncontradoCodigo, String inventarioNoEncontradoDescripcion) {
        this.cuenta = cuentaCliente;
        this.codigo = inventarioNoEncontradoCodigo;
        this.descripcion = inventarioNoEncontradoDescripcion;
    }
}
