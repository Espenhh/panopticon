package pro.panopticon.client.sensor.impl;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.model.MetricDimension;
import pro.panopticon.client.sensor.Sensor;
import pro.panopticon.client.util.SystemStatus;

import java.util.*;

public class MemorySensor implements Sensor {
    private static final String DESCRIPTION = "When this alarm is triggered, you should check the memory status of the other nodes as well. " +
            "There might be a memory leak somewhere in the application triggering this, so a restart will buy you some time";
    private static final long BYTES_IN_MB = 1024 * 1024;

    private final int warnLimitNow;
    private final int errorLimitNow;
    private final int warnLimitHeap;
    private final int errorLimitHeap;
    private final Optional<String> hostname;

    public MemorySensor() {
        this(85, 95, 75, 95);
    }

    public MemorySensor(int warnLimitNow, int errorLimitNow, int warnLimitHeap, int errorLimitHeap) {
        this(warnLimitNow, errorLimitNow, warnLimitHeap, errorLimitHeap, null);
    }

    public MemorySensor(int warnLimitNow, int errorLimitNow, int warnLimitHeap, int errorLimitHeap, String hostname) {
        this.warnLimitNow = warnLimitNow;
        this.errorLimitNow = errorLimitNow;
        this.warnLimitHeap = warnLimitHeap;
        this.errorLimitHeap = errorLimitHeap;
        this.hostname = Optional.ofNullable(hostname);
    }

    @Override
    public List<Measurement> measure() {
        SystemStatus s = new SystemStatus();

        List<Measurement> measurements = new ArrayList<>();

        putMemoryStatus(measurements, "mem.heap.now", s.heapUsed(), s.heapMax(), warnLimitNow, errorLimitNow);
        putMemoryStatus(measurements, "mem.heap.lastGC", s.heapAfterGC(), s.heapMax(), warnLimitHeap, errorLimitHeap);

        return measurements;
    }

    private void putMemoryStatus(List<Measurement> measurements, String key, long used, long max, int warnLimit, int errorLimit) {

        if (max == 0 || used == -1) {
            return;
        }
        long percentUsed = used / (max / 100);

        String displayValue = toMB(used) + " of " + toMB(max) + " MB (" + percentUsed + "%)";

        List<MetricDimension> dimensions = hostname
                .map(h -> Collections.singletonList(MetricDimension.instanceDimension(h)))
                .orElse(Collections.emptyList());

        measurements.add(
                new Measurement(
                        key,
                        status(percentUsed, warnLimit, errorLimit),
                        displayValue,
                        new Measurement.CloudwatchValue(
                                percentUsed,
                                StandardUnit.Percent,
                                dimensions
                                ),
                        DESCRIPTION));
    }

    private String status(long percentUsed, int warnLimit, int errorLimit) {
        if (percentUsed > errorLimit) {
            return "ERROR";
        } else if (percentUsed > warnLimit) {
            return "WARN";
        } else {
            return "INFO";
        }
    }

    private long toMB(final long bytes) {
        return bytes / BYTES_IN_MB;
    }
}
