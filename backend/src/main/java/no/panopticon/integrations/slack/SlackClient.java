package no.panopticon.integrations.slack;

import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;
import no.panopticon.alerters.MissingRunningUnitsAlerter;
import no.panopticon.config.SlackConfiguration;
import no.panopticon.integrations.cloudwatch.CloudWatchUrlUtils;
import no.panopticon.storage.RunningUnit;
import no.panopticon.storage.StatusSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class SlackClient {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    public static final String RED = "#E32D22";
    public static final String YELLOW = "#F8E71C";
    public static final String GREEN = "#69CA14";

    private final SlackSession slack;
    private final SlackConfiguration slackConfiguration;
    private String previousCombinedStatusAlertMessageTimestamp;
    private List<Line> previousCombinedAlerts = new ArrayList<>();

    @Autowired
    public SlackClient(SlackConfiguration slackConfiguration) {
        slack = SlackSessionFactory.createWebSocketSlackSession(slackConfiguration.token);
        this.slackConfiguration = slackConfiguration;
        connectIfNessesary();
    }

    private void connectIfNessesary() {
        if (!slack.isConnected()) {
            try {
                slack.connect();
            } catch (IOException e) {
                LOG.error("Could not connect to slack", e);
            }
        }
    }

    public void indicateMissingRunningUnit(RunningUnit runningUnit) {
        slackMessage(slackConfiguration.channel, runningUnit, RED, "Instansen har ikke rapportert inn status de siste 5 minuttene. Kan tyde på at noe er galt. Sjekk ut om den fortsatt kjører.");
    }

    public void indicateReturnedRunningUnit(RunningUnit runningUnit) {
        slackMessage(slackConfiguration.channel, runningUnit, GREEN, "Instansen som var forsvunnet har nå rapportert inn status igjen. Back in business, baby!");
    }

    public void alertAboutStatus(RunningUnit unit, StatusSnapshot.Measurement measurement) {
        String color = GREEN;
        if (measurement.getStatus().equals("WARN")) color = YELLOW;
        if (measurement.getStatus().equals("ERROR")) color = RED;
        String message = measurement.getKey() + ": " + measurement.getDisplayValue();
        slackMessage(slackConfiguration.channelDetailed, unit, color, message);
    }

    private void slackMessage(String channelName, RunningUnit runningUnit, String color, String text) {
        connectIfNessesary();

        SlackChannel channel = slack.findChannelByName(channelName);

        String name = String.format("[%s] %s på %s", runningUnit.getEnvironment().toUpperCase(), runningUnit.getComponent(), runningUnit.getServer());

        SlackAttachment attachment = new SlackAttachment(name, "", text, null);
        attachment.setColor(color);
        attachment.setFooter("Se alle detaljer i Panopticon: " + slackConfiguration.panopticonurl);
        attachment.addMarkdownIn("text, footer");

        SlackPreparedMessage message = new SlackPreparedMessage.Builder()
                .addAttachment(attachment)
                .build();
        slack.sendMessage(channel, message);
    }

    public synchronized void combinedStatusAlerting(List<Line> alertLines) {
        if (previousCombinedAlerts.equals(alertLines)) {
            return;
        }

        connectIfNessesary();

        SlackChannel channel = slack.findChannelByName(slackConfiguration.channel);

        String heading = String.format("*%d målepunkter med feil akkurat nå. Se detaljert hendelseslogg i #%s*", alertLines.size(), slackConfiguration.channelDetailed);

        List<SlackAttachment> attachments = alertLines.stream().map(l -> {
            SlackAttachment a = new SlackAttachment(
                    l.header,
                    null,
                    String.format(
                            "%s\n\n<%s|Se logger i CloudWatch>",
                            l.message,
                            CloudWatchUrlUtils.getCloudWatchUrl(l.component, l.alertKey)
                    ),
                    null
            );
            if (l.severity.equals("ERROR")) {
                a.setColor(RED);
            } else if (l.severity.equals("WARN")) {
                a.setColor(YELLOW);
            }
            a.addMarkdownIn("text");
            return a;
        }).collect(toList());

        SlackPreparedMessage message = new SlackPreparedMessage.Builder()
                .withMessage(heading)
                .withLinkNames(true)
                .withAttachments(attachments)
                .build();

        if (previousCombinedStatusAlertMessageTimestamp != null) {
            slack.deleteMessage(previousCombinedStatusAlertMessageTimestamp, channel);
        }

        SlackMessageHandle<SlackMessageReply> reply = slack.sendMessage(channel, message);
        previousCombinedStatusAlertMessageTimestamp = reply.getReply().getTimestamp();
        previousCombinedAlerts = alertLines;

    }

    public void indicateNoRunningUnits(MissingRunningUnitsAlerter.Component c) {
        connectIfNessesary();

        SlackChannel channel = slack.findChannelByName(slackConfiguration.channel);

        String name = String.format("[%s] %s", c.getEnvironment().toUpperCase(), c.getComponent());

        String text = String.format("Ingen aktive servere for denne appen i panopticon! Sjekk om det er nede, eller om det bare er feil med målingene...");
        SlackAttachment attachment = new SlackAttachment(name, "", text, null);
        attachment.setColor(YELLOW);
        attachment.setFooter("Se alle detaljer i Panopticon: " + slackConfiguration.panopticonurl);
        attachment.addMarkdownIn("text, footer");

        SlackPreparedMessage message = new SlackPreparedMessage.Builder()
                .addAttachment(attachment)
                .build();
        slack.sendMessage(channel, message);
    }

    public static class Line {

        private final String severity;
        private final String header;
        private final String message;
        private final String component;
        private final String alertKey;

        public Line(String severity, String header, String message, String component, String alertKey) {
            this.severity = severity;
            this.header = header;
            this.message = message;
            this.component = component;
            this.alertKey = alertKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Line line = (Line) o;

            if (severity != null ? !severity.equals(line.severity) : line.severity != null) return false;
            if (header != null ? !header.equals(line.header) : line.header != null) return false;
            return message != null ? message.equals(line.message) : line.message == null;
        }

        @Override
        public int hashCode() {
            int result = severity != null ? severity.hashCode() : 0;
            result = 31 * result + (header != null ? header.hashCode() : 0);
            result = 31 * result + (message != null ? message.hashCode() : 0);
            return result;
        }
    }
}
