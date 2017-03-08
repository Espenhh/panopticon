package no.panopticon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pagerduty")
public class PagerdutyConfiguration {

    public String apikey;

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }
}
