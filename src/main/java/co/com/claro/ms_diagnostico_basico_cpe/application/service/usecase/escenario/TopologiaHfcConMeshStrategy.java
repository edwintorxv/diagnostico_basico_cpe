package co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.escenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import co.com.claro.ms_diagnostico_basico_cpe.application.service.usecase.ConsultarParametrosDipositivosService;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.utils.HelperMesh;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.validator.CanalWifiValidator;
import co.com.claro.ms_diagnostico_basico_cpe.application.service.validator.MacAdreesValidator;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.acs.DeviceParamsResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.diagnostico.DiagnosticoResponse;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorClienteDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.InventarioPorTopoligiaDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.model.dto.poller.ResponseArpPollerDto;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.acs.IAcsPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.domain.port.out.poller.IPollerPortOut;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.Constantes;
import co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants.configuration.ConstantsMessageResponse;

@Service
public class TopologiaHfcConMeshStrategy implements DiagnosticoTopologiaHfcStrategy {

	private final ConsultarParametrosDipositivosService consultarParametrosDipositivosService;
	private final MacAdreesValidator macAdreesValidator;
	private final CanalWifiValidator canalWifiValidator;

	public TopologiaHfcConMeshStrategy(CanalWifiValidator canalWifiValidator,
			ConsultarParametrosDipositivosService consultarParametrosDipositivosService,
			MacAdreesValidator macAdreesValidator) {
		this.canalWifiValidator = canalWifiValidator;
		this.consultarParametrosDipositivosService = consultarParametrosDipositivosService;
		this.macAdreesValidator = macAdreesValidator;
	}

	@Override
	public DiagnosticoResponse diagnosticar(InventarioPorTopoligiaDto topologia, IPollerPortOut pollerPortOut,
			IAcsPortOut acsPortOut) throws Exception {

		String cuentaCliente = topologia.getCuentaCliente();
		try {
			InventarioPorClienteDto cpePrincipal = topologia.getInventarioCPE();
			List<InventarioPorClienteDto> meshList = Optional.ofNullable(topologia.getLstinventarioMesh())
					.orElseGet(List::of);

			if (cpePrincipal == null) {
				return HelperMesh.errorInventario(cuentaCliente);
			}

			List<InventarioPorClienteDto> equipos = new ArrayList<>();
			equipos.add(cpePrincipal);
			equipos.addAll(meshList);

			List<String> seriales = equipos.stream().map(HelperMesh::serialInventarioNormalizado) // toma serialNumber o
																									// serialMac
					.filter(Objects::nonNull).toList();

			List<ResponseArpPollerDto> listaArp = pollerPortOut
					.consultarARP(HelperMesh.formatMacAddress(topologia.getInventarioCPE().getSerialMac()));

			if (listaArp == null || listaArp.isEmpty()) {
				return HelperMesh.diagnostico(cuentaCliente,
						Constantes.HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_CODIGO,
						Constantes.HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_DESCRIPCION);
			}

			List<ResponseArpPollerDto> coincidencias = listaArp.stream().filter(arpItem -> {
				boolean estadoActivo = "Activo".equalsIgnoreCase(arpItem.getStatus());
				String macArpSinFormato = arpItem.getMac().replace(":", "").toUpperCase();
				boolean macCoincide = seriales.contains(macArpSinFormato);
				return estadoActivo && macCoincide;
			}).collect(Collectors.toList());

			long numeroDeCoincidencias = coincidencias.size();

			if (numeroDeCoincidencias == 1) {

				ResponseArpPollerDto apMaestroEncontrado = coincidencias.get(0);
				String macMaster = apMaestroEncontrado.getMac();

				String macSinPuntos = HelperMesh.unformatMacAddress(macMaster);

				// 5) Consultar parámetros AP maestro
				Optional<InventarioPorClienteDto> maestroEncontradoInv = topologia.getLstinventarioMesh().stream()
						.filter(equipo -> equipo.getSerialNumber() != null
								&& equipo.getSerialNumber().equals(macSinPuntos.toUpperCase()))
						.findFirst();

				boolean wifiMaestro = false;

				if (maestroEncontradoInv.isPresent()) {

					InventarioPorClienteDto meshMaster = maestroEncontradoInv.get();

					// Validar estado AP maestro en ACS
					if (!HelperMesh.estaOnline(meshMaster.getSerialMac(), acsPortOut)) {
						return HelperMesh.diagnostico(cuentaCliente,
								Constantes.HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_CODIGO,
								Constantes.HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_DESCRIPCION);
					}

					// 5) Consultar parámetros AP maestro
					DeviceParamsDto dtoMesh = HelperMesh.buildDeviceParamsDto(HelperMesh.serialPreferido(meshMaster),
							Constantes.KEY_OR_TREE_MESH, HelperMesh.safeLower(meshMaster.getMarca()),
							meshMaster.getModelo());

					DeviceParamsResponse responseMesh = consultarParametrosDipositivosService
							.consultarParametrosDispositivo(dtoMesh);

					if (HelperMesh.isAcsDataEmpty(responseMesh)) {
						return HelperMesh.diagnostico(cuentaCliente, Constantes.ACS_NO_REPORTA_DATA_CODIGO,
								Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION);
					} else {
						wifiMaestro = true;
					}

					List<String> macsMesh = HelperMesh.normalizarListaMac(
							macAdreesValidator.macAddressDetectadas(responseMesh, dtoMesh.getKeyOrTree()));

					// 6) Buscar AP esclavo
					InventarioPorClienteDto meshSlave = HelperMesh.findInventarioCoincidente(equipos, macsMesh);
					if (meshSlave == null) {
						return HelperMesh.errorInventario(cuentaCliente);
					}

					// Consultar AP esclavo
					DeviceParamsDto dtoSlave = HelperMesh.buildDeviceParamsDto(HelperMesh.serialPreferido(meshSlave),
							Constantes.KEY_OR_TREE_MESH, meshMaster.getMarca(), meshMaster.getModelo());

					DeviceParamsResponse responseSlave = consultarParametrosDipositivosService
							.consultarParametrosDispositivo(dtoSlave);
					if (HelperMesh.isAcsDataEmpty(responseSlave)) {
						return HelperMesh.diagnostico(cuentaCliente, Constantes.ACS_NO_REPORTA_DATA_CODIGO,
								Constantes.ACS_NO_REPORTA_DATA_DESCRIPCION);
					}

					boolean wifiSlaveOk = canalWifiValidator.validar(responseSlave, dtoSlave.getKeyOrTree());

					// 7) Diagnósticos finales de canales
					if (!wifiSlaveOk) {
						return HelperMesh.diagnostico(cuentaCliente,
								Constantes.HFC_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_CODIGO,
								Constantes.HFC_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_DESCRIPCION);
					}

					if (wifiMaestro && wifiSlaveOk) {
						return HelperMesh.diagnostico(cuentaCliente,
								Constantes.HFC_ONLINE_CON_ULTRAWIFI_CANALES_ONLINE_AP_ONT_CODIGO,
								Constantes.HFC_ONLINE_CON_ULTRAWIFI_CANALES_ONLINE_AP_ONT_DESCRIPCION);
					}
					if (!wifiMaestro && wifiSlaveOk) {
						return HelperMesh.diagnostico(cuentaCliente,
								Constantes.HFC_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_ONT_ONLINE_AP_CODIGO,
								Constantes.HFC_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_ONT_ONLINE_AP_DESCRIPCION);
					}

				}

			} else if (numeroDeCoincidencias > 1) {
				return HelperMesh.diagnostico(cuentaCliente, Constantes.HFC_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_CODIGO,
						Constantes.HFC_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_DESCRIPCION);

			} else if (numeroDeCoincidencias == 0) {
				return HelperMesh.diagnostico(cuentaCliente,
						Constantes.HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_CODIGO,
						Constantes.HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_DESCRIPCION);
			}

			return HelperMesh.errorInventario(cuentaCliente);

		} catch (Exception e) {
			return new DiagnosticoResponse("OK", ConstantsMessageResponse.REQUEST_PROCESSED_SUCCESSFULLY,
					List.of(new DiagnosticoDto(cuentaCliente, Constantes.INVENTARIO_NO_ENCONTRADO_CODIGO,
							String.format(Constantes.INVENTARIO_NO_ENCONTRADO_DESCRIPCION, cuentaCliente))));

		}

	}

}
