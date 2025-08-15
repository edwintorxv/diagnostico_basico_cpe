package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.utils;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 29/11/2024
 */
public enum ERequestRest {
    EXAMPLE {
        @Override
        public String request() {
            return "{\"propertie\": \"%s\",\"propertie2\": \"%s\",\"propertie3\": \"%s\",\"propertie4\": \"%s\"}";
        }
    },CHANGE_DEVICE_ONT_INTERFACE{
        @Override
        public String request() {
            return "{\r\n"
            		+ "    \"serviceCode\": \"%s\",\r\n"
            		+ "    \"swHostname\": \"%s\",\r\n"
            		+ "    \"swInterface\": \"%s\",\r\n"
            		+ "    \"newSerial\": \"%s\"\r\n"
            		+ "}";
        }
    },CHANGE_DEVICE_ONT{
    	@Override
        public String request() {
            return "{\r\n"
            		+ "    \"serviceCode\": \"%s\",\r\n"
            		+ "    \"newSerial\": \"%s\"\r\n"
            		+ "}";
        }
    	
    };

    public abstract String request();
}
