package pro.panopticon.client.sensor.impl;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class SuccessrateSensor implements Sensor {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final int numberToKeep;
    private final Double warnLimit;
    private final Double errorLimit;
    private final Map<String, CircularFifoQueue<Event>> eventQueues = new HashMap<>();

    public SuccessrateSensor(int numberToKeep, Double warnLimit, Double errorLimit) {
        this.numberToKeep = numberToKeep;
        this.warnLimit = warnLimit;
        this.errorLimit = errorLimit;
    }

    public void tickSuccess(String key) {
        try {
            getQueueForKey(key).add(Event.SUCCESS);
        } catch (Exception e) {
            LOG.warn("Something went wrong when counting SUCCESS for " + key, e);
        }
    }

    public void tickFailure(String key) {
        try {
            getQueueForKey(key).add(Event.FAILURE);
        } catch (Exception e) {
            LOG.warn("Something went wrong when counting FAILURE for " + key, e);
        }
    }

    private CircularFifoQueue<Event> getQueueForKey(String key) {
        return eventQueues.computeIfAbsent(key, k -> new CircularFifoQueue<>(numberToKeep));
    }

    @Override
    public List<Measurement> measure() {
        return eventQueues.entrySet().stream()
                .map((Map.Entry<String, CircularFifoQueue<Event>> e) -> {
                    List<Event> events = e.getValue().stream().collect(toList());
                    int all = events.size();
                    long success = events.stream().filter(a -> a == Event.SUCCESS).count();
                    long failure = events.stream().filter(a -> a == Event.FAILURE).count();
                    double percentFailureDouble = all > 0 ? (double) failure / (double) all : 0;
                    boolean enoughDataToAlert = all == numberToKeep;
                    String display = String.format("Last %s calls: %s success, %s failure (%.2f%% failure)%s",
                            Integer.min(all, numberToKeep),
                            success,
                            all-success,
                            percentFailureDouble * 100,
                            enoughDataToAlert ? "" : " - not enough calls to report status yet"
                    );
                    return new Measurement(e.getKey(), getStatusFromPercentage(enoughDataToAlert, percentFailureDouble), display, new Measurement.CloudwatchValue(percentFailureDouble * 100, StandardUnit.Percent));
                })
                .collect(toList());
    }

    private String getStatusFromPercentage(boolean enoughDataToAlert, double percentFailure) {
        if (!enoughDataToAlert) return "INFO";
        if (errorLimit != null && percentFailure >= errorLimit) return "ERROR";
        if (warnLimit != null && percentFailure >= warnLimit) return "WARN";
        return "INFO";
    }

    private enum Event {
        SUCCESS,
        FAILURE
    }

}
