package pro.panopticon.client.sensor.impl;

import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;
import pro.panopticon.client.util.SystemStatus;

import java.util.ArrayList;
import java.util.List;

public class FileHandlesSensor implements Sensor {
    @Override
    public List<Measurement> measure() {
        SystemStatus s = new SystemStatus();

        List<Measurement> measurements = new ArrayList<>();

        String displayValue = s.openFileHandles() + " handles";
        measurements.add(new Measurement("filehandles", "INFO", displayValue, s.openFileHandles()));

        return measurements;
    }
}
