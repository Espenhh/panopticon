package no.panopticon.integrations.slack;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatDeleteRequest;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.Attachment;
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
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@Service
public class SlackClient {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    public static final String RED = "#E32D22";
    public static final String YELLOW = "#F8E71C";
    public static final String GREEN = "#69CA14";

    private final Slack slack;
    private final SlackConfiguration slackConfiguration;
    private String previousCombinedStatusAlertMessageTimestamp;
    private List<Line> previousCombinedAlerts = new ArrayList<>();

    @Autowired
    public SlackClient(SlackConfiguration slackConfiguration) {
        slack = Slack.getInstance();
        this.slackConfiguration = slackConfiguration;
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

    private void slackMessage(String channelId, RunningUnit runningUnit, String color, String text) {
        String name = String.format("[%s] %s på %s", runningUnit.getEnvironment().toUpperCase(), runningUnit.getComponent(), runningUnit.getServer());

        Attachment attachment = Attachment.builder()
                .title(name)
                .text(text)
                .color(color)
                .footer("Se alle detaljer i Panopticon: " + slackConfiguration.panopticonurl)
                .mrkdwnIn(asList("text", "footer"))
                .build();

        ChatPostMessageRequest message = ChatPostMessageRequest.builder()
                .channel(channelId)
                .attachments(singletonList(attachment))
                .build();


        try {
            slack.methods(slackConfiguration.token).chatPostMessage(message);
        } catch (IOException | SlackApiException e) {
            LOG.warn("Unable to send message to Slack.", e);
        }
    }

    public synchronized void combinedStatusAlerting(List<Line> alertLines) {
        if (previousCombinedAlerts.equals(alertLines)) {
            return;
        }

        String heading = String.format(
                "*%d %s med feil akkurat nå. Se detaljert hendelseslogg i #%s*",
                alertLines.size(),
                alertLines.size() == 1 ? "målepunkt" : "målepunkter",
                slackConfiguration.channelDetailed
        );

        List<Attachment> attachments = alertLines.stream().map(l -> {
            String message;
            if (l.component != null && l.alertKey != null) {
                message = String.format(
                        "%s\n\n<%s|Se logger i CloudWatch>",
                        l.message,
                        CloudWatchUrlUtils.getCloudWatchUrl(l.component, l.alertKey)
                );
            } else {
                message = l.message;
            }

            Attachment.AttachmentBuilder attachmentBuilder = Attachment.builder()
                    .title(l.header)
                    .text(message)
                    .mrkdwnIn(singletonList("text"));

            if (l.severity.equals("ERROR")) {
                attachmentBuilder.color(RED);
            } else if (l.severity.equals("WARN")) {
                attachmentBuilder.color(YELLOW);
            }
            return attachmentBuilder.build();
        }).collect(toList());

        ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .channel(slackConfiguration.channel)
                .text(heading)
                .linkNames(true)
                .attachments(attachments)
                .build();

        if (previousCombinedStatusAlertMessageTimestamp != null) {
            try {
                slack.methods(slackConfiguration.token).chatDelete(ChatDeleteRequest.builder()
                        .channel(slackConfiguration.channel)
                        .ts(previousCombinedStatusAlertMessageTimestamp)
                        .build()
                );
            } catch (IOException | SlackApiException e) {
                LOG.warn("Unable to delete existing status message.", e);
            }
        }

        try {
            ChatPostMessageResponse chatPostMessageResponse = slack.methods(slackConfiguration.token).chatPostMessage(request);
            previousCombinedStatusAlertMessageTimestamp = chatPostMessageResponse.getTs();
            previousCombinedAlerts = alertLines;

        } catch (IOException | SlackApiException e) {
            LOG.warn("Unable to post status message to Slack.", e);
        }
    }

    public void indicateNoRunningUnits(MissingRunningUnitsAlerter.Component c) {
        String name = String.format("[%s] %s", c.getEnvironment().toUpperCase(), c.getComponent());

        Attachment attachment = Attachment.builder()
                .title(name)
                .text("Ingen aktive servere for denne appen i panopticon! Sjekk om det er nede, eller om det bare er feil med målingene...")
                .color(YELLOW)
                .footer("Se alle detaljer i Panopticon: " + slackConfiguration.panopticonurl)
                .mrkdwnIn(asList("text", "footer"))
                .build();

        ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                .channel(slackConfiguration.channel)
                .attachments(singletonList(attachment))
                .build();

        try {
            slack.methods(slackConfiguration.token).chatPostMessage(request);
        } catch (IOException | SlackApiException e) {
            LOG.warn("Unable to post 'no running units' message to Slack", e);
        }
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

            if (!Objects.equals(severity, line.severity)) return false;
            if (!Objects.equals(header, line.header)) return false;
            return Objects.equals(message, line.message);
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
