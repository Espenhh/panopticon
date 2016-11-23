package no.panopticon.client.sensor.impl;

import no.panopticon.client.model.Measurement;
import no.panopticon.client.sensor.Sensor;
import no.panopticon.client.util.SystemStatus;

import java.util.ArrayList;
import java.util.List;

public class MemorySensor implements Sensor {

    private static final long BYTES_IN_MB = 1024 * 1024;

    @Override
    public List<Measurement> measure() {
        SystemStatus s = new SystemStatus();

        List<Measurement> measurements = new ArrayList<>();

        putMemoryStatus(measurements, "mem.heap.now", s.heapUsed(), s.heapMax());
        putMemoryStatus(measurements, "mem.heap.lastGC", s.heapAfterGC(), s.heapMax());

        return measurements;
    }

    private void putMemoryStatus(List<Measurement> measurements, String key, long used, long max) {

        if (max == 0 || used == -1) {
            return;
        }
        long percentUsed = used / (max / 100);
        long percentLeft = 100 - percentUsed;

        String displayValue = toMB(used) + " of " + toMB(max) + " MB (" + percentUsed + "%)";

        measurements.add(new Measurement(key, status(percentLeft), displayValue, 0));
    }

    private String status(long percentLeft) {
        if (percentLeft < 5) {
            return "ERROR";
        } else if (percentLeft < 25) {
            return "WARN";
        } else {
            return "INFO";
        }
    }

    private long toMB(final long bytes) {
        return bytes / BYTES_IN_MB;
    }
}
