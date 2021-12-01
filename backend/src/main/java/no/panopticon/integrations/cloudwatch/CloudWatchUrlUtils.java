package no.panopticon.integrations.cloudwatch;

import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Component
public class CloudWatchUrlUtils {
    private static final String BASE_URL = "https://eu-central-1.console.aws.amazon.com/cloudwatch/home?region=eu-central-1";

    private static final Map<String, String> LOG_GROUP_MAP  = new HashMap<String, String>() {{
        put("booking", "/aws/elasticbeanstalk/vy-booking-prod/var/log/eb-docker/containers/eb-current-app/stdouterr.log");
        put("deviation", "/aws/elasticbeanstalk/vy-deviation-prod/var/log/eb-docker/containers/eb-current-app/stdouterr.log");
        put("entertainment", "/aws/elasticbeanstalk/vy-entertainment-prod/var/log/eb-docker/containers/eb-current-app/stdouterr.log");
        put("itinerary", "/aws/elasticbeanstalk/vy-itinerary-prod/var/log/eb-docker/containers/eb-current-app/stdouterr.log");
        put("location", "/aws/elasticbeanstalk/vy-location-prod/var/log/eb-docker/containers/eb-current-app/stdouterr.log");
        put("loyalty", "/aws/lambda/prod-loyalty-s3-listener-lambda");
        put("notification", "/aws/elasticbeanstalk/vy-notification-prod/var/log/eb-docker/containers/eb-current-app/stdouterr.log");
        put("pdfTicket", "/aws/elasticbeanstalk/vy-pdf-ticket-prod/var/log/eb-docker/containers/eb-current-app/stdouterr.log");
        put("seat", "/aws/elasticbeanstalk/vy-seat-prod/var/log/eb-docker/containers/eb-current-app/stdouterr.log");
        put("taxi", "/aws/elasticbeanstalk/vy-taxi-prod/var/log/eb-docker/containers/eb-current-app/stdouterr.log");
        put("ticket", "/aws/elasticbeanstalk/nsb-ticket-prod/var/log/eb-docker/containers/eb-current-app/stdouterr.log");
        put("user", "/aws/elasticbeanstalk/vy-user-prod/var/log/eb-docker/containers/eb-current-app/stdouterr.log");
        put("user-data", "/aws/elasticbeanstalk/vy-user-data-prod/var/log/eb-docker/containers/eb-current-app/stdouterr.log");
    }};

    /** Gets a custom CloudWatch URL for a given component and search string */
    public static String getCloudWatchUrl(String component, String searchString) {
        return String.format(
                "%s#logsV2:log-groups/log-group/%s/log-events%s",
                BASE_URL,
                getLogGroup(component),
                getFilterOptions(searchString)
        );
    }

    /** Gets the correct URL for a given log group
     *
     * This function runs the URL encoding algorithm twice and replaces all % with $. This is the way CloudWatch URLs
     * are generated for some reason.
     * */
    private static String getLogGroup(String component) {
        return urlEncodeString(
                urlEncodeString(
                        LOG_GROUP_MAP.getOrDefault(component, "/aws/elasticbeanstalk")
                )
            ).replaceAll("%", "\\$");
    }

    private static String getFilterOptions(String searchString) {
        return urlEncodeString("?start=-3600000&filterPattern=" + searchString)
                .replaceAll("%", "\\$");
    }

    private static String urlEncodeString(String original) {
        try {
            return URLEncoder.encode(original, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return original;
        }
    }
}

