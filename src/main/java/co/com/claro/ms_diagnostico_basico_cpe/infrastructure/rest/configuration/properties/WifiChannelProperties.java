package co.com.claro.ms_diagnostico_basico_cpe.infrastructure.rest.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "wifi")
public class WifiChannelProperties {

    // Ahora Spring parsea directamente cada valor separado por coma en una List<String>
    private Map<String, List<String>> channels;

    public Map<String, List<String>> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, List<String>> channels) {
        this.channels = channels;
    }

    public List<String> getChannelsForKey(String key) {
        return channels.getOrDefault(key, List.of());
    }

    @PostConstruct
    public void init() {
        System.out.println("===== PROPIEDADES CARGADAS =====");
        channels.forEach((k, v) -> System.out.println(k + " -> " + v));
    }
}
