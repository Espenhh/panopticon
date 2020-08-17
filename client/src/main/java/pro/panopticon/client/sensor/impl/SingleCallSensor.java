package pro.panopticon.client.sensor.impl;

import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SingleCallSensor implements Sensor {

    private final Map<AlertInfo, Status> measurements;

    public SingleCallSensor() {
        this.measurements = new HashMap<>();
    }

    @Override
    public List<Measurement> measure() {
        return measurements.entrySet().stream().map((Map.Entry<AlertInfo, Status> it) ->
                new Measurement(
                        it.getKey().getSensorKey(),
                        getPanopticonStatus(it.getValue()),
                        getDisplayValue(it.getValue()),
                        it.getKey().getDescription()
                )
        ).collect(Collectors.toList());

    }

    public void triggerError(AlertInfo alertInfo) {
        measurements.compute(alertInfo, (k, v) -> Status.ERROR);
    }

    public void triggerOk(AlertInfo alertInfo) {
        measurements.compute(alertInfo, (k, v) -> Status.OK);
    }

    private String getPanopticonStatus(Status status) {
        switch (status) {
            case ERROR:
                return "ERROR";
            case OK:
                return "INFO";
            default:
                return "";
        }
    }

    private String getDisplayValue(Status status) {
        if (status == Status.ERROR) {
            return "In error";
        }
        return "";
    }

    private enum Status {
        OK, ERROR
    }
}
