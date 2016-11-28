package pro.panopticon.client.sensor;

import pro.panopticon.client.model.Measurement;

import java.util.List;

@FunctionalInterface
public interface Sensor {

    List<Measurement> measure();

}
