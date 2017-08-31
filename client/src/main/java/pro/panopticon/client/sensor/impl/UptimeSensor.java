package pro.panopticon.client.sensor.impl;

import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UptimeSensor implements Sensor {

    private static final LocalDateTime STARTED = LocalDateTime.now();

    @Override
    public List<Measurement> measure() {
        List<Measurement> measurements = new ArrayList<>();

        measurements.add(new Measurement("uptime.since", "INFO", STARTED.toString()));

        return measurements;
    }
}
