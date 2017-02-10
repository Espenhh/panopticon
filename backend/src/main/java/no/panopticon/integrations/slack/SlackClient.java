package no.panopticon.integrations.slack;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import no.panopticon.config.SlackConfiguration;
import no.panopticon.storage.RunningUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SlackClient {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    public static final String RED = "#E32D22";
    public static final String YELLOW = "#F8E71C";
    public static final String GREEN = "#69CA14";

    private final SlackSession slack;
    private final SlackConfiguration slackConfiguration;

    @Autowired
    public SlackClient(SlackConfiguration slackConfiguration) {
        slack = SlackSessionFactory.createWebSocketSlackSession(slackConfiguration.token);
        this.slackConfiguration = slackConfiguration;
        connectIfNessesary();
    }

    private void connectIfNessesary() {
        if(!slack.isConnected()) {
            try {
                slack.connect();
            } catch (IOException e) {
                LOG.error("Could not connect to slack", e);
            }
        }
    }

    public void indicateMissingRunningUnit(RunningUnit runningUnit) {
        slackMessage(runningUnit, RED, "Instansen har ikke rapportert inn status de siste 5 minuttene. Kan tyde på at noe er galt. Sjekk ut om den fortsatt kjører.");
    }

    public void indicateReturnedRunningUnit(RunningUnit runningUnit) {
        slackMessage(runningUnit, GREEN, "Instansen som var forsvunnet har nå rapportert inn status igjen. Back in business, baby!");
    }

    private void slackMessage(RunningUnit runningUnit, String color, String text) {
        connectIfNessesary();

        SlackChannel channel = slack.findChannelByName(slackConfiguration.channel);

        String name = String.format("[%s] %s på %s", runningUnit.getEnvironment().toUpperCase(), runningUnit.getComponent(), runningUnit.getServer());

        SlackAttachment attachment = new SlackAttachment(name, "", text, null);
        attachment.setColor(color);
        attachment.setFooter("Se alle detaljer i Panopticon: http://nsb.panopticon.pro");
        attachment.addMarkdownIn("text, footer");

        SlackPreparedMessage message = new SlackPreparedMessage.Builder()
                .addAttachment(attachment)
                .build();
        slack.sendMessage(channel, message);
    }
}
