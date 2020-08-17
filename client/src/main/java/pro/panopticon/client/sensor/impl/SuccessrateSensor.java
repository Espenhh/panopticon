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

    /**
     * Number of events to keep.
     */
    private final int numberToKeep;

    /**
     * Triggers an alert to Slack when reached.
     * Should always be a Double between 0.00 and 1.00
     * Format: percentage / 100
     * <p>
     * Example: 0.1 will trigger a warning at 10% failure rate
     */
    private final Double warnLimit;

    /**
     * Triggers an alert to Slack and PagerDuty when reached.
     * Should always be a Double between 0.00 and 1.00
     * Format: percentage / 100
     * <p>
     * Example: 0.2 will trigger an alert at 20% failure rate
     */
    private final Double errorLimit;

    private final Map<AlertInfo, CircularFifoQueue<Event>> eventQueues = new HashMap<>();

    public SuccessrateSensor(int numberToKeep, Double warnLimit, Double errorLimit) {
        this.numberToKeep = numberToKeep;
        this.warnLimit = warnLimit;
        this.errorLimit = errorLimit;
    }

    @Deprecated
    public synchronized void tickSuccess(String key) {
        tickSuccess(new AlertInfo(key, null));
    }

    @Deprecated
    public synchronized void tickFailure(String key) {
        tickFailure(new AlertInfo(key, null));
    }

    public synchronized void tickFailure(AlertInfo alertInfo) {
        try {
            getQueueForKey(alertInfo).add(Event.FAILURE);
        } catch (Exception e) {
            LOG.warn("Something went wrong when counting FAILURE for " + alertInfo.getSensorKey(), e);
        }
    }

    public synchronized void tickSuccess(AlertInfo alertInfo) {
        try {
            getQueueForKey(alertInfo).add(Event.SUCCESS);
        } catch (Exception e) {
            LOG.warn("Something went wrong when counting SUCCESS for " + alertInfo.getSensorKey(), e);
        }
    }

    private CircularFifoQueue<Event> getQueueForKey(AlertInfo key) {
        return eventQueues.computeIfAbsent(key, k -> new CircularFifoQueue<>(numberToKeep));
    }

    @Override
    public List<Measurement> measure() {
        return eventQueues.entrySet().stream()
                .map((Map.Entry<AlertInfo, CircularFifoQueue<Event>> e) -> {
                    AlertInfo alertInfo = e.getKey();
                    List<Event> events = e.getValue().stream().collect(toList());
                    int all = events.size();
                    long success = events.stream().filter(a -> a == Event.SUCCESS).count();
                    long failure = events.stream().filter(a -> a == Event.FAILURE).count();
                    double percentFailureDouble = all > 0 ? (double) failure / (double) all : 0;
                    boolean enoughDataToAlert = all == numberToKeep;
                    String display = String.format("Last %s calls: %s success, %s failure (%.2f%% failure)%s",
                            Integer.min(all, numberToKeep),
                            success,
                            all - success,
                            percentFailureDouble * 100,
                            enoughDataToAlert ? "" : " - not enough calls to report status yet"
                    );
                    return new Measurement(alertInfo.getSensorKey(), getStatusFromPercentage(enoughDataToAlert, percentFailureDouble), display, new Measurement.CloudwatchValue(percentFailureDouble * 100, StandardUnit.Percent), alertInfo.getDescription());
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
