package no.panopticon.client.sensor;

import no.panopticon.client.model.Measurement;

import java.util.List;

@FunctionalInterface
public interface Sensor {

    List<Measurement> measure();

}
