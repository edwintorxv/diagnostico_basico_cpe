package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.utils;

/**
 * @author <a href="mailto:betancourtks@globalhitss.com">Sebastian Betancourt</a> on 6/12/2024
 */
public enum ERequestSoap {

    EXAMPLE {
        @Override
        public String request() {
            return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                    "xmlns:ex=\"http://example.service.com\">" +
                    "<soapenv:Header/>" +
                    "<soapenv:Body>" +
                    "<ex:exampleRequest>" +
                    "<ex:propertie>%s</ex:propertie>" +
                    "<ex:propertie2>%s</ex:propertie2>" +
                    "<ex:propertie3>%s</ex:propertie3>" +
                    "<ex:propertie4>%s</ex:propertie4>" +
                    "</ex:exampleRequest>" +
                    "</soapenv:Body>" +
                    "</soapenv:Envelope>";
        }
    };

    public abstract String request();
}
