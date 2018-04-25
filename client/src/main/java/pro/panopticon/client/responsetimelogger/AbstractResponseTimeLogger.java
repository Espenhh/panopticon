package pro.panopticon.client.responsetimelogger;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import pro.panopticon.client.awscloudwatch.CloudwatchClient;
import pro.panopticon.client.awscloudwatch.CloudwatchClient.CloudwatchStatistic;
import pro.panopticon.client.awscloudwatch.HasCloudwatchConfig;
import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;

import java.util.*;

public class AbstractResponseTimeLogger implements Sensor {

    private final String namespace;


    // Using Vector since it's syncronized, e.g thread safe
    private Vector<CloudwatchStatistic> counts = new Vector<>();

    private CloudwatchClient cloudwatchClient;

    public AbstractResponseTimeLogger(HasCloudwatchConfig hasCloudwatchConfig, CloudwatchClient cloudwatchClient) {
        this.cloudwatchClient = cloudwatchClient;
        this.namespace = String.format("responsetimes-%s-%s", hasCloudwatchConfig.getAppName(), hasCloudwatchConfig.getEnvironment());
    }

    public void addResponseTimeMeasurement(String name, long timeInMilliseconds) {
        counts.add(new CloudwatchStatistic(name, (double) timeInMilliseconds, StandardUnit.Milliseconds, new Date()));
    }

    @Override
    public List<Measurement> measure() {
        List<CloudwatchStatistic> toSubmit = this.counts;
        counts = new Vector<>();

        cloudwatchClient.sendStatistics(namespace, toSubmit);

        // This sensor returns no measurements for now – it's just implemented as a sensor for similarity to the other concepts
        return Collections.emptyList();
    }

}
