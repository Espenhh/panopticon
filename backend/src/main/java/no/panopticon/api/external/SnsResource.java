package no.panopticon.api.external;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import no.panopticon.integrations.slack.SlackClient;
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

        if (snsMessage.Type.equals("Notification")) {
            String logMsgAndSubject = ">>Notification received from topic " + snsMessage.TopicArn;
            if (snsMessage.Subject != null) {
                logMsgAndSubject += " Subject: " + snsMessage.Subject;
            }
            logMsgAndSubject += " Message: " + snsMessage.Message;
            LOG.info(logMsgAndSubject);
            slackClient.awsSnsNotificationToSlack(snsMessage);
        } else if (snsMessage.Type.equals("SubscriptionConfirmation")) {
            try {
                //Confirm the subscription by going to the subscribeURL location
                //and capture the return value (XML message body as a string)
                Scanner sc = new Scanner(new URL(snsMessage.SubscribeURL).openStream());
                StringBuilder sb = new StringBuilder();
                while (sc.hasNextLine()) {
                    sb.append(sc.nextLine());
                }
                LOG.info(">>Subscription confirmation (" + snsMessage.SubscribeURL + ") Return value: " + sb.toString());
                slackClient.awsSnsNotificationToSlack(snsMessage);
            } catch (IOException e) {
                LOG.error(">>Unable to confirm the subscription", e);
                return Response.status(BAD_GATEWAY).build();
            }
        } else if (snsMessage.Type.equals("UnsubscribeConfirmation")) {
            LOG.info(">>Unsubscribe confirmation: " + snsMessage.Message);
        } else {
            LOG.info(">>Unknown message type.");
        }
        LOG.info(">>Done processing message: " + snsMessage.MessageId);

        return Response.status(CREATED).build();
    }

}
