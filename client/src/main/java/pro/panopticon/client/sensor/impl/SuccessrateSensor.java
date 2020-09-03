package pro.panopticon.client.sensor.impl;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;
import pro.panopticon.client.util.NowSupplier;
import pro.panopticon.client.util.NowSupplierImpl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class SuccessrateSensor implements Sensor {

    public static final int HOURS_FOR_ERROR_TICK_TO_BE_CONSIDERED_OUTDATED = 1;
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

    private final Map<AlertInfo, CircularFifoQueue<Tick>> eventQueues = new HashMap<>();
    private final NowSupplier nowSupplier;

    public SuccessrateSensor(int numberToKeep, Double warnLimit, Double errorLimit) {
        this.numberToKeep = numberToKeep;
        this.warnLimit = warnLimit;
        this.errorLimit = errorLimit;
        this.nowSupplier = new NowSupplierImpl();
    }

    SuccessrateSensor(int numberToKeep, Double warnLimit, Double errorLimit, NowSupplier nowSupplier) {
        this.numberToKeep = numberToKeep;
        this.warnLimit = warnLimit;
        this.errorLimit = errorLimit;
        this.nowSupplier = nowSupplier;
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
            getQueueForKey(alertInfo).add(new Tick(Event.FAILURE, nowSupplier.now()));
        } catch (Exception e) {
            LOG.warn("Something went wrong when counting FAILURE for " + alertInfo.getSensorKey(), e);
        }
    }

    public synchronized void tickSuccess(AlertInfo alertInfo) {
        try {
            getQueueForKey(alertInfo).add(new Tick(Event.SUCCESS, nowSupplier.now()));
        } catch (Exception e) {
            LOG.warn("Something went wrong when counting SUCCESS for " + alertInfo.getSensorKey(), e);
        }
    }

    private CircularFifoQueue<Tick> getQueueForKey(AlertInfo key) {
        return eventQueues.computeIfAbsent(key, k -> new CircularFifoQueue<>(numberToKeep));
    }

    @Override
    public List<Measurement> measure() {
        return eventQueues.entrySet().stream()
                .map(this::measure)
                .collect(toList());
    }

    private Measurement measure(Map.Entry<AlertInfo, CircularFifoQueue<Tick>> e) {
        AlertInfo alertInfo = e.getKey();
        List<Tick> ticks = new ArrayList<>(e.getValue());
        int all = ticks.size();
        long success = ticks.stream().filter(tick -> tick.event == Event.SUCCESS).count();
        long failure = ticks.stream().filter(tick -> tick.event == Event.FAILURE).count();
        double percentFailureDouble = all > 0 ? (double) failure / (double) all : 0;
        boolean enoughDataToAlert = all == numberToKeep;
        boolean hasRecentErrorTicks = hasRecentErrorTicks(ticks);
        String display = String.format("Last %s calls: %s success, %s failure (%.2f%% failure)%s%s",
                Integer.min(all, numberToKeep),
                success,
                all - success,
                percentFailureDouble * 100,
                enoughDataToAlert ? "" : " - not enough calls to report status yet",
                hasRecentErrorTicks ? "" : " - no recent error ticks"
        );
        String status = decideStatus(enoughDataToAlert, percentFailureDouble, hasRecentErrorTicks);
        return new Measurement(alertInfo.getSensorKey(), status, display, new Measurement.CloudwatchValue(percentFailureDouble * 100, StandardUnit.Percent), alertInfo.getDescription());
    }

    private String decideStatus(boolean enoughDataToAlert, double percentFailure, boolean hasRecentErrorTicks) {
        if (!enoughDataToAlert) return "INFO";
        if (!hasRecentErrorTicks) return "INFO";
        if (errorLimit != null && percentFailure >= errorLimit) return "ERROR";
        if (warnLimit != null && percentFailure >= warnLimit) return "WARN";
        return "INFO";
    }

    private boolean hasRecentErrorTicks(List<Tick> events) {
        return events.stream()
                .filter(tick -> tick.event == Event.FAILURE)
                .anyMatch(tick -> ChronoUnit.HOURS.between(tick.createdAt, nowSupplier.now()) < HOURS_FOR_ERROR_TICK_TO_BE_CONSIDERED_OUTDATED);
    }

    private enum Event {
        SUCCESS,
        FAILURE
    }

    private static class Tick {
        private final Event event;
        private final LocalDateTime createdAt;

        private Tick(Event event, LocalDateTime createdAt) {
            this.event = event;
            this.createdAt = createdAt;
        }
    }

}
