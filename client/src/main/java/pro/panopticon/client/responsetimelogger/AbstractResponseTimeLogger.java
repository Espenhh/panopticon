package pro.panopticon.client.responsetimelogger;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import pro.panopticon.client.awscloudwatch.CloudwatchClient;
import pro.panopticon.client.awscloudwatch.CloudwatchClient.CloudwatchStatistic;
import pro.panopticon.client.awscloudwatch.HasCloudwatchConfig;
import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AbstractResponseTimeLogger implements Sensor {

    private final String namespace;

    private ConcurrentMap<String, List<CloudwatchStatistic>> counts = new ConcurrentHashMap<>();

    private CloudwatchClient cloudwatchClient;

    public AbstractResponseTimeLogger(HasCloudwatchConfig hasCloudwatchConfig, CloudwatchClient cloudwatchClient) {
        this.cloudwatchClient = cloudwatchClient;
        this.namespace = String.format("responsetimes-%s-%s", hasCloudwatchConfig.getAppName(), hasCloudwatchConfig.getEnvironment());
    }

    public void addResponseTimeMeasurement(String name, long timeInMilliseconds) {
        counts.computeIfAbsent(name, n -> new ArrayList<>())
                .add(new CloudwatchStatistic(name, (double) timeInMilliseconds, StandardUnit.Milliseconds, new Date()));
    }

    @Override
    public List<Measurement> measure() {
        ConcurrentMap<String, List<CloudwatchStatistic>> toSubmit = counts;
        counts = new ConcurrentHashMap<>();
        toSubmit.forEach((key, measurements) -> cloudwatchClient.sendStatistics(namespace, measurements));

        // This sensor returns no measurements for now – it's just implemented as a sensor for similarity to the other concepts
        return Collections.emptyList();
    }

}
