package pro.panopticon.client.eventlogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.panopticon.client.awscloudwatch.CloudwatchClient;
import pro.panopticon.client.awscloudwatch.HasCloudwatchConfig;
import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class AbstractEventLogger implements Sensor {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    ConcurrentMap<String, DoubleAdder> counts = new ConcurrentHashMap<>();

    private HasCloudwatchConfig hasCloudwatchConfig;
    private CloudwatchClient cloudwatchClient;

    public AbstractEventLogger(HasCloudwatchConfig hasCloudwatchConfig, CloudwatchClient cloudwatchClient) {
        this.hasCloudwatchConfig = hasCloudwatchConfig;
        this.cloudwatchClient = cloudwatchClient;
    }

    public void tickAndLog(HasEventInfo event, String... logappends) {
        tickAndLog(event, 1, logappends);
    }

    public void tickAndLog(HasEventInfo event, double count, String... logappends) {
        performLog(event, logappends);
        performTick(event, count);
    }

    public void tick(HasEventInfo event) {
        tick(event, 1);
    }

    public void tick(HasEventInfo event, double count) {
        performTick(event, count);
    }

    private void performLog(HasEventInfo event, String... logappends) {
        LOG.info("AUDIT EVENT - [" + event.getEventType() + "] - [" + event.getEventName() + "] - " + Stream.of(logappends).map(s -> "[" + s + "]").collect(joining(" - ")));
    }

    private void performTick(HasEventInfo event, double count) {
        counts.computeIfAbsent(event.getEventName(), s -> new DoubleAdder()).add(count);
    }

    @Override
    public List<Measurement> measure() {
        ConcurrentMap<String, DoubleAdder> mapToProcess = counts;
        counts = new ConcurrentHashMap<>();

        if (cloudwatchClient != null && hasCloudwatchConfig != null && hasCloudwatchConfig.auditeventStatisticsEnabled()) {
            List<CloudwatchClient.CloudwatchStatistic> statistics = mapToProcess.entrySet().stream()
                    .map(e -> new CloudwatchClient.CloudwatchStatistic(e.getKey(), e.getValue().doubleValue()))
                    .collect(toList());
            cloudwatchClient.sendStatistics(hasCloudwatchConfig.auditeventStatisticsNamespace(), statistics);
        }

        return mapToProcess.entrySet().stream()
                .map(e -> new Measurement("audit." + e.getKey(), "INFO", "Last minute: " + e.getValue().doubleValue()))
                .collect(toList());
    }
}
