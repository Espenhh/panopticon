package no.panopticon.api.external;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import no.panopticon.api.external.sns.*;
import no.panopticon.integrations.slack.SlackClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.*;

@Component
@Path("/api/external/sns")
public class SnsResource {

    public static final String NOTIFICATION = "Notification";
    public static final String SUBSCRIPTION_CONFIRMATION = "SubscriptionConfirmation";
    public static final String UNSUBSCRIBE_CONFIRMATION = "UnsubscribeConfirmation";
    private final SlackClient slackClient;

    @Autowired
    public SnsResource(SlackClient slackClient) {
        this.slackClient = slackClient;
    }

    private static final Logger LOG = LoggerFactory.getLogger(SnsResource.class);

    @POST
    @Consumes(TEXT_PLAIN)
    @Produces(TEXT_PLAIN)
    public Response processSnsMessage(String message) {
        LOG.info("Received sns message: " + message);
        JSONObject jsonObj = new JSONObject(message);
        Gson gson = new GsonBuilder().create();
        SnsMessage snsMessage = gson.fromJson(jsonObj.toString(), SnsMessage.class);

        if (snsMessage.Type == null) {
            return Response.status(NO_CONTENT).build();
        }

        String topic = snsMessage.TopicArn.substring(snsMessage.TopicArn.lastIndexOf(':') + 1).trim();

        if (snsMessage.Type.equals(NOTIFICATION)) {
            logNotification(snsMessage);
            String color = SlackClient.GREEN;
            if (snsMessage.Message.contains("AlarmName")) {
                Alarm alarm = gson.fromJson(snsMessage.Message, Alarm.class);
                if ("ALARM".equals(alarm.NewStateValue)) {
                    color = SlackClient.RED;
                }
                slackClient.awsSnsNotificationToSlack(alarm.NewStateValue, alarm.AlarmDescription, alarm.NewStateReason, topic, color);
            } else {
                slackClient.awsSnsNotificationToSlack(snsMessage.Type, snsMessage.Subject, snsMessage.Message, topic, color);
            }
        } else if (snsMessage.Type.equals(SUBSCRIPTION_CONFIRMATION)) {
            try {
                //Confirm the subscription by going to the subscribeURL location
                //and capture the return value (XML message body as a string)
                Scanner sc = new Scanner(new URL(snsMessage.SubscribeURL).openStream());
                StringBuilder sb = new StringBuilder();
                while (sc.hasNextLine()) {
                    sb.append(sc.nextLine());
                }
                LOG.info(">>{} ({}) Return value: {}", SUBSCRIPTION_CONFIRMATION, snsMessage.SubscribeURL, sb.toString());
                if (snsMessage.Subject == null) {
                    snsMessage.Subject = topic;
                }
                slackClient.awsSnsNotificationToSlack(snsMessage.Type, snsMessage.Subject, snsMessage.Message + "\nReturn value: " + sb.toString(), topic, SlackClient.GREEN);
            } catch (IOException e) {
                slackClient.awsSnsNotificationToSlack(snsMessage.Type, snsMessage.Subject, snsMessage.Message, topic, SlackClient.RED);
                LOG.error(">>Unable to confirm the subscription", e);
                return Response.status(BAD_GATEWAY).build();
            }
        } else if (snsMessage.Type.equals(UNSUBSCRIBE_CONFIRMATION)) {
            LOG.info(">>{}: {}", UNSUBSCRIBE_CONFIRMATION, snsMessage.Message);
            slackClient.awsSnsNotificationToSlack(snsMessage.Type, snsMessage.Subject, snsMessage.Message, topic, SlackClient.GREEN);
        } else {
            LOG.info(">>Unknown message type.");
            slackClient.awsSnsNotificationToSlack(snsMessage.Type, snsMessage.Subject, "Unknown message type.", topic, SlackClient.YELLOW);
        }
        LOG.info(">>Done processing message: {}", snsMessage.MessageId);

        return Response.status(CREATED).build();
    }

    private void logNotification(SnsMessage snsMessage) {
        String logMsgAndSubject = ">>Notification received from topic " + snsMessage.TopicArn;
        if (snsMessage.Subject != null) {
            logMsgAndSubject += " Subject: " + snsMessage.Subject;
        }
        logMsgAndSubject += " Message: " + snsMessage.Message;
        LOG.info(logMsgAndSubject);
    }


}
