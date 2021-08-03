package no.panopticon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "slack")
public class SlackConfiguration {

    public String token;

    public String channel;

    public String channelDetailed;

    public String channelDetailedName;

    public String panopticonurl;

    public void setToken(String token) {
        this.token = token;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setChannelDetailed(String channelDetailed) {
        this.channelDetailed = channelDetailed;
    }

    public void setChannelDetailedName(String channelDetailedName) {
        this.channelDetailedName = channelDetailedName;
    }

    public void setPanopticonurl(String panopticonurl) {
        this.panopticonurl = panopticonurl;
    }

}
