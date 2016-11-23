package no.panopticon.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.panopticon.client.model.ComponentInfo;
import no.panopticon.client.model.Measurement;
import no.panopticon.client.model.Status;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

public class PanopticonClient {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    private final String baseUri;
    private CloseableHttpClient client;

    public PanopticonClient(String baseUri) {
        this.baseUri = baseUri;
        client = HttpClientBuilder.create().build();
    }

    public boolean update(Status status) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(status);
            String uri = baseUri + "/external/status";

            LOG.debug("Updating status: " + uri);
            LOG.debug("...with JSON: " + json);

            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(json.getBytes()));

            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");

            CloseableHttpResponse response = client.execute(httpPost);

            LOG.debug("Response: " + response.getStatusLine().getStatusCode());

            return response.getStatusLine().getStatusCode() < 300;
        } catch (IOException e) {
            LOG.warn("Error when updating status", e);
            return false;
        }
    }

    public void startScheduledStatusUpdate(ComponentInfo componentInfo, List<Supplier<List<Measurement>>> sensors) {
        Runnable runnable = () -> {
            long before = System.currentTimeMillis();
            List<Measurement> measurements = sensors.parallelStream()
					.map(Supplier::get)
					.flatMap(List::stream)
					.collect(toList());
            long afterMeasurements = System.currentTimeMillis();
            boolean success = update(new Status(componentInfo, measurements));
            long afterStatusPost = System.currentTimeMillis();

            long measurementTime = afterMeasurements-before;
            long statuspostTime = afterStatusPost-afterMeasurements;

            if(success) {
                LOG.info("Sent status update with " + measurements.size() + " measurements. Fetch measurements took " + measurementTime + "ms. Posting status took " + statuspostTime + "ms.");
            } else {
                LOG.warn("Could not update status");
            }
        };
        SCHEDULER.scheduleWithFixedDelay(runnable, 0, 1, TimeUnit.MINUTES);
    }
}
