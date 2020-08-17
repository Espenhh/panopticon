package pro.panopticon.client.sensor.impl;

import org.junit.Test;
import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;

import java.util.List;

import static org.junit.Assert.*;

public class SingleCallSensorTest {

    public final String ALARM_DESCRIPTION = "Alarm description";

    @Test
    public void returns_empty_list_of_measurements_when_no_measurements() {
        SingleCallSensor sensor = new SingleCallSensor();

        List<Measurement> measurements = sensor.measure();

        assertNotNull("Measure should return a non-null list", measurements);
    }

    @Test
    public void returns_list_of_measurements() {
        SingleCallSensor sensor = new SingleCallSensor();

        sensor.triggerError(new Sensor.AlertInfo("123", ALARM_DESCRIPTION));
        List<Measurement> measurements = sensor.measure();

        assertEquals("Returns as many measurements as registered", 1, measurements.size());
    }

    @Test
    public void changes_status_when_error_triggered() {
        SingleCallSensor sensor = new SingleCallSensor();

        sensor.triggerError(new Sensor.AlertInfo("123", ALARM_DESCRIPTION));
        List<Measurement> measurements = sensor.measure();

        Measurement measurement = getMeasurementFromList(measurements, "123");
        assertEquals("Measurement has error status", "ERROR", measurement.status);
    }

    @Test
    public void returns_measurement_with_description() {
        SingleCallSensor sensor = new SingleCallSensor();

        sensor.triggerError(new Sensor.AlertInfo("123", ALARM_DESCRIPTION));
        List<Measurement> measurements = sensor.measure();

        Measurement measurement = getMeasurementFromList(measurements, "123");
        assertEquals( ALARM_DESCRIPTION, measurement.description);
    }

    @Test
    public void only_one_measurement_for_key() {
        SingleCallSensor sensor = new SingleCallSensor();

        sensor.triggerError(new Sensor.AlertInfo("123", ALARM_DESCRIPTION));
        sensor.triggerError(new Sensor.AlertInfo("123", ALARM_DESCRIPTION));
        sensor.triggerOk(new Sensor.AlertInfo("456", ALARM_DESCRIPTION));
        sensor.triggerOk(new Sensor.AlertInfo("456", ALARM_DESCRIPTION));
        List<Measurement> measurements = sensor.measure();

        assertEquals(2, measurements.size());
    }

    @Test
    public void changes_status_back_to_ok() {
        SingleCallSensor sensor = new SingleCallSensor();

        sensor.triggerError(new Sensor.AlertInfo("123", ALARM_DESCRIPTION));
        sensor.triggerOk(new Sensor.AlertInfo("123", ALARM_DESCRIPTION));
        List<Measurement> measurements = sensor.measure();

        Measurement measurement = getMeasurementFromList(measurements, "123");
        assertEquals("Measurement has info status", "INFO", measurement.status);
    }

    @Test
    public void changes_status_back_to_error() {
        SingleCallSensor sensor = new SingleCallSensor();

        sensor.triggerOk(new Sensor.AlertInfo("123", ALARM_DESCRIPTION));
        sensor.triggerError(new Sensor.AlertInfo("123", ALARM_DESCRIPTION));
        List<Measurement> measurements = sensor.measure();

        Measurement measurement = getMeasurementFromList(measurements, "123");
        assertEquals("Measurement has error status", "ERROR", measurement.status);
    }

    private Measurement getMeasurementFromList(List<Measurement> measurements, String key) {
        return measurements.stream()
                .filter(it -> key.equals(it.key))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

}
