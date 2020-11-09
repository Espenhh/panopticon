package pro.panopticon.client.eventlogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.panopticon.client.awscloudwatch.CloudwatchClient;
import pro.panopticon.client.awscloudwatch.HasCloudwatchConfig;
import pro.panopticon.client.model.MetricDimension;
import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class AbstractEventLogger implements Sensor {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final String namespace;

    private ConcurrentMap<String, DoubleAdder> counts = new ConcurrentHashMap<>();
    private Vector<CloudwatchClient.CloudwatchStatistic> ticksWithDimensions = new Vector<>();

    private HasCloudwatchConfig hasCloudwatchConfig;
    private CloudwatchClient cloudwatchClient;

    public AbstractEventLogger(HasCloudwatchConfig hasCloudwatchConfig, CloudwatchClient cloudwatchClient) {
        this.hasCloudwatchConfig = hasCloudwatchConfig;
        this.cloudwatchClient = cloudwatchClient;
        this.namespace = String.format("audit-%s-%s", hasCloudwatchConfig.getAppName(), hasCloudwatchConfig.getEnvironment());
    }

    public void tickAndLog(HasEventInfo event, String... logappends) {
        tickAndLog(event, 1, logappends);
    }

    public void tickAndLogException(HasEventInfo event, Exception e) {
        tickAndLog(event, 1, stackTraceToString(e));
    }

    private String stackTraceToString(Exception e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
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

    public void tick(HasEventInfo event, MetricDimension... dimensions) {
        CloudwatchClient.CloudwatchStatistic statistics = new CloudwatchClient.CloudwatchStatistic(
                event.getEventName(),
                1.0
        ).withDimensions(Arrays.asList(dimensions));
        ticksWithDimensions.add(statistics);
    }

    public void tick(HasEventInfo event, double count, MetricDimension... dimensions) {
        CloudwatchClient.CloudwatchStatistic statistics = new CloudwatchClient.CloudwatchStatistic(
                event.getEventName(),
                count
        ).withDimensions(Arrays.asList(dimensions));
        ticksWithDimensions.add(statistics);
    }

    private void performLog(HasEventInfo event, String... logappends) {
        LOG.info("AUDIT EVENT - [" + event.getEventType() + "] - [" + event.getEventName() + "] - " + Stream.of(logappends).map(s -> "[" + s + "]").collect(joining(" - ")));
    }

    private void performTick(HasEventInfo event, double count) {
        counts.computeIfAbsent(event.getEventName(), s -> new DoubleAdder()).add(count);
    }

    @Override
    public List<Measurement> measure() {
        ConcurrentMap<String, DoubleAdder> countsToProcess = this.counts;
        Vector<CloudwatchClient.CloudwatchStatistic> countsWithDimensionToProcess = this.ticksWithDimensions;
        resetTicks();

        if (statisticsEnabled()) {
            List<CloudwatchClient.CloudwatchStatistic> statistics = createCountStatistics(countsToProcess);
            statistics.addAll(countsWithDimensionToProcess);
            cloudwatchClient.sendStatistics(namespace, statistics);
        }

        return countsToProcess.entrySet().stream()
                .map(e -> new Measurement("audit." + e.getKey(), "INFO", "Last minute: " + e.getValue().doubleValue(), ""))
                .collect(toList());
    }

    private boolean statisticsEnabled() {
        return cloudwatchClient != null && hasCloudwatchConfig != null && hasCloudwatchConfig.auditeventStatisticsEnabled();
    }

    private void resetTicks() {
        this.counts = new ConcurrentHashMap<>();
        this.ticksWithDimensions = new Vector<>();
    }

    private List<CloudwatchClient.CloudwatchStatistic> createCountStatistics(ConcurrentMap<String, DoubleAdder> countsToProcess) {
        return countsToProcess.entrySet().stream()
                .map(e -> new CloudwatchClient.CloudwatchStatistic(e.getKey(), e.getValue().doubleValue()))
                .collect(toList());
    }
}
