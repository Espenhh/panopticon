package pro.panopticon.client.sensor.impl;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;
import pro.panopticon.client.util.SystemStatus;

import java.util.ArrayList;
import java.util.List;

public class FileHandlesSensor implements Sensor {

    private final long warnAfter;
    private final long errorAfter;

    public FileHandlesSensor(long warnAfter, long errorAfter) {
        this.warnAfter = warnAfter;
        this.errorAfter = errorAfter;
    }

    @Override
    public List<Measurement> measure() {
        SystemStatus s = new SystemStatus();

        List<Measurement> measurements = new ArrayList<>();

        long open = s.openFileHandles();
        long max = s.maxFileHandles();
        double percent = ((double) open / (double) max) * 100;
        String displayValue = String.format("%s of %s filehandles used (%.2f%%)", open, max, percent);

        measurements.add(new Measurement("filehandles", statusFromOpenFileHandles(open), displayValue, new Measurement.CloudwatchValue(percent, StandardUnit.Percent)));

        return measurements;
    }

    private String statusFromOpenFileHandles(long open) {
        if (open >= errorAfter) {
            return "ERROR";
        } else if (open >= warnAfter) {
            return "WARN";
        } else {
            return "INFO";
        }
    }

}
