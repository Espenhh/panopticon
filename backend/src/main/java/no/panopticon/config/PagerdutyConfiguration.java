package no.panopticon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "pagerduty")
public class PagerdutyConfiguration {

    public String integrationKey;
    public String apikey;
    public String environmentAlertWhitelist;

    public void setApikey(String apikey) {
        this.apikey = apikey;
    }

    public void setEnvironmentAlertWhitelist(String environmentAlertWhitelist) {
        this.environmentAlertWhitelist = environmentAlertWhitelist;
    }

    public void setIntegrationKey(String integrationKey) {
        this.integrationKey = integrationKey;
    }
}
