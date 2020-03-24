package pro.panopticon.client.sensor.impl;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;
import pro.panopticon.client.util.SystemStatus;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ServerLoadSensor implements Sensor {
    private static final String DESCRIPTION = "";
    @Override
    public List<Measurement> measure() {
        SystemStatus s = new SystemStatus();

        List<Measurement> measurements = new ArrayList<>();

        double load = s.load();
        String formatted = new DecimalFormat("#.##").format(load);
        String status;
        if (load > 10) {
            status = "ERROR";
        } else if (load > 5) {
            status = "WARN";
        } else {
            status = "INFO";
        }
        measurements.add(new Measurement("load.avg", status, formatted, new Measurement.CloudwatchValue(load, StandardUnit.None), DESCRIPTION));

        return measurements;
    }
}
