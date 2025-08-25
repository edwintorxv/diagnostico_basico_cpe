package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.constants;

public class Constantes {

    public static final String REQUEST_MAPPING = "diagres/diagnosticoBasicoCPE";

    public static final String INVENTARIO_NO_ENCONTRADO_CODIGO = "600";
    public static final String INVENTARIO_NO_ENCONTRADO_DESCRIPCION = "La cuenta ingresada %s no existe ";

    //Mensajes de respuesta por validación
    public static final String ONT_NO_ONLINE_CODIGO = "300";
    public static final String ONT_NO_ONLINE_DESCRIPCION = "No es posible consultar la ONT.";

    public static final String ONT_ONLINE_SIN_ULTRAWIFI_CANALES_ONLINE_CODIGO = "601";
    public static final String ONT_ONLINE_SIN_ULTRAWIFI_CANALES_ONLINE_DESCRIPCION = "Topología de ONT correcta";

    public static final String ONT_ONLINE_SIN_ULTRAWIFI_CANALES_OFFLINE_CODIGO = "602";
    public static final String ONT_ONLINE_SIN_ULTRAWIFI_CANALES_OFFLINE_DESCRIPCION = "Canales deshabilitados en ONT";

    public static final String ONT_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_CODIGO = "603";
    public static final String ONT_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_DESCRIPCION = "Se detecta problema en Topología por más de 1 AP Maestro";

    public static final String ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_CODIGO = "604";
    public static final String ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_DESCRIPCION = "No se detecta UltraWiFi operativo, se debe validar daño y/o garantía de este";

    public static final String ONT_ONLINE_CON_ULTRAWIFI_SIN_AP_ESCLAVO_CODIGO = "605";
    public static final String ONT_ONLINE_CON_ULTRAWIFI_SIN_AP_ESCLAVO_DESCRIPCION = "No se encuentra AP Esclavo en la topología";

    public static final String ONT_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_CODIGO = "606";
    public static final String ONT_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_DESCRIPCION = "El UltraWifi no tiene encendidos ambos canales";

    public static final String ONT_ONLINE_CON_ULTRAWIFI_CANALES_ONLINE_AP_ONT_CODIGO = "607";
    public static final String ONT_ONLINE_CON_ULTRAWIFI_CANALES_ONLINE_AP_ONT_DESCRIPCION = "Se sugiere deshabilitar Canal 2.4 GHz y 5GHz en ONT";

    public static final String ONT_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_ONT_ONLINE_AP_CODIGO = "608";
    public static final String ONT_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_ONT_ONLINE_AP_DESCRIPCION = "Topología de ONT y UltraWiFi Correcta";

    public static final String KEY_OR_TREE_ONT = "diagres-diagbasic-ont-%s-%s";
    public static final String KEY_OR_TREE_MESH = "diagres-diagbasic-meshs-%s-%s";
    
    public static final String HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_CODIGO = "304";
    public static final String HFC_ONLINE_CON_ULTRAWIFI_NO_DETECTADA_APMAESTRO_DESCRIPCION = "No se detecta UltraWiFi operativo, Se debe validar daño y/o garantía del mismo";
    
    public static final String HFC_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_CODIGO = "303";
    public static final String HFC_ONLINE_CON_ULTRAWIFI_MAS_DE_DOS_MAC_DESCRIPCION = "Se detecta problema en Topología por más de 1 AP Maestro";
    
    public static final String HFC_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_CODIGO = "306";
    public static final String HFC_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_DESCRIPCION = "Se deberá informar que el UltraWifi no tiene encendidos ambos canales. Se debe validar con el cliente estado de cobertura WiFi";
    
    public static final String HFC_ONLINE_CON_ULTRAWIFI_CANALES_ONLINE_AP_ONT_CODIGO = "307";
    public static final String HFC_ONLINE_CON_ULTRAWIFI_CANALES_ONLINE_AP_ONT_DESCRIPCION = "Se sugiere deshabilitar Canal 2.4 GHz y 5GHz en ONT";
    
    public static final String HFC_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_ONT_ONLINE_AP_CODIGO = "308";
    public static final String HFC_ONLINE_CON_ULTRAWIFI_CANALES_OFFLINE_ONT_ONLINE_AP_DESCRIPCION = "Topología de ONT y UltraWiFi Correcta";
    
    public static final String ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTA_AP_MAESTRO_CODIGO = "600";
    public static final String ONT_ONLINE_CON_ULTRAWIFI_NO_DETECTA_AP_MAESTRO_DESCRIPCION = "No se encuentra el AP maestro";

    public static final String ACS_NO_REPORTA_DATA_CODIGO = "600";
    public static final String ACS_NO_REPORTA_DATA_DESCRIPCION = "ACS no reporta data";



}
