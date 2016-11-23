package no.panopticon.client.sensor.impl;

import no.panopticon.client.model.Measurement;
import no.panopticon.client.sensor.Sensor;

import java.util.ArrayList;
import java.util.List;

public class JavaVersionSensor implements Sensor {

    private final String version = System.getProperty("java.version");

    @Override
    public List<Measurement> measure() {
        List<Measurement> measurements = new ArrayList<>();
        measurements.add(new Measurement("java.version", "INFO", version, 0));
        return measurements;
    }
}
