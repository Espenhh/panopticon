package no.panopticon.client.sensor.impl;

import no.panopticon.client.model.Measurement;
import no.panopticon.client.sensor.Sensor;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.singletonList;

public class ReportTimeSensor implements Sensor {
    @Override
    public List<Measurement> measure() {
        return singletonList(
                new Measurement("report.generated", "INFO", LocalDateTime.now().toString(), 0)
        );
    }
}
