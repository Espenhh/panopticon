package no.panopticon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "slack")
public class SlackConfiguration {

    public String token;

    public String channel;

    public String panopticonurl;

    public void setToken(String token) {
        this.token = token;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setPanopticonurl(String panopticonurl) {
        this.panopticonurl = panopticonurl;
    }
}
