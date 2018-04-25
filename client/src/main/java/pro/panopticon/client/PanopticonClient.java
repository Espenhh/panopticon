package pro.panopticon.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.panopticon.client.awscloudwatch.CloudwatchClient;
import pro.panopticon.client.awscloudwatch.HasCloudwatchConfig;
import pro.panopticon.client.model.ComponentInfo;
import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.model.Status;
import pro.panopticon.client.sensor.Sensor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toList;

public class PanopticonClient {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private static final int TIMEOUT = 10_000;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    private final String baseUri;
    private final HasCloudwatchConfig hasCloudwatchConfig;
    private final CloudwatchClient cloudwatchClient;
    private final CloseableHttpClient client;
    private final String namespace;

    public PanopticonClient(String baseUri, HasCloudwatchConfig hasCloudwatchConfig, CloudwatchClient cloudwatchClient) {
        this.baseUri = baseUri;
        this.hasCloudwatchConfig = hasCloudwatchConfig;
        this.cloudwatchClient = cloudwatchClient;
        client = createHttpClient();
        this.namespace = String.format("sensor-%s-%s", hasCloudwatchConfig.getAppName(), hasCloudwatchConfig.getEnvironment());
    }

    public void startScheduledStatusUpdate(ComponentInfo componentInfo, List<Sensor> sensors) {
        SCHEDULER.scheduleWithFixedDelay(() -> performSensorCollection(componentInfo, sensors), 0, 1, TimeUnit.MINUTES);
    }

    public void shutdownScheduledStatusUpdate() {
        SCHEDULER.shutdown();
    }

    private void performSensorCollection(ComponentInfo componentInfo, List<Sensor> sensors) {
        try {
            long before = System.currentTimeMillis();
            List<Measurement> measurements = collectMeasurementsFromSensors(sensors);
            long afterMeasurements = System.currentTimeMillis();

            boolean success = sendMeasurementsToPanopticon(new Status(componentInfo, measurements));
            long afterPanopticonPost = System.currentTimeMillis();

            sendSelectMeasurementsToCloudwatch(measurements);
            long afterCloudwatchPost = System.currentTimeMillis();

            long measurementTime = afterMeasurements - before;
            long panopticonPostTime = afterPanopticonPost - afterMeasurements;
            long cloudwatchPostTime = afterCloudwatchPost - afterPanopticonPost;

            if (success) {
                LOG.info(String.format("Sent status update with %d measurements. Fetch measurements took %dms. Posting status to panopticon took %dms. Posting to cloudwatch took %dms", measurements.size(), measurementTime, panopticonPostTime, cloudwatchPostTime));
            } else {
                LOG.warn("Could not update status");
            }
        } catch (Exception e) {
            LOG.warn("Got error when measuring sensors to send to panopticon", e);
        }
    }

    private List<Measurement> collectMeasurementsFromSensors(List<Sensor> sensors) {
        return sensors.parallelStream()
                .map((sensor) -> {
                    try {
                        return sensor.measure();
                    } catch (Exception e) {
                        LOG.warn("Got error running sensor: " + sensor.getClass().getName(), e);
                        return new ArrayList<Measurement>();
                    }
                })
                .flatMap(List::stream)
                .collect(toList());
    }

    boolean sendMeasurementsToPanopticon(Status status) {
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

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                LOG.debug("Response: " + response.getStatusLine().getStatusCode());
                return response.getStatusLine().getStatusCode() < 300;
            }
        } catch (IOException e) {
            LOG.warn("Error when updating status", e);
            return false;
        }
    }

    private void sendSelectMeasurementsToCloudwatch(List<Measurement> measurements) {
        List<CloudwatchClient.CloudwatchStatistic> statistics = measurements.stream()
                .filter(m -> m.cloudwatchValue != null)
                .map(m -> new CloudwatchClient.CloudwatchStatistic(m.key, m.cloudwatchValue.value, m.cloudwatchValue.unit))
                .collect(toList());
        if (cloudwatchClient != null && hasCloudwatchConfig != null && hasCloudwatchConfig.sensorStatisticsEnabled()) {
            cloudwatchClient.sendStatistics(namespace, statistics);
        }
    }

    private CloseableHttpClient createHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(TIMEOUT)
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .build();

        return HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }
}
