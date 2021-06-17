package pro.panopticon.client.sensor.impl

import org.junit.Assert
import org.junit.Test
import pro.panopticon.client.model.Measurement
import pro.panopticon.client.sensor.Sensor

class SingleCallSensorTest {
    val ALARM_DESCRIPTION = "Alarm description"

    @Test
    fun returns_empty_list_of_measurements_when_no_measurements() {
        val sensor = SingleCallSensor()
        val measurements = sensor.measure()
        Assert.assertNotNull("Measure should return a non-null list", measurements)
    }

    @Test
    fun returns_list_of_measurements() {
        val sensor = SingleCallSensor()
        sensor.triggerError(Sensor.AlertInfo("123", ALARM_DESCRIPTION))
        val measurements = sensor.measure()
        Assert.assertEquals("Returns as many measurements as registered", 1, measurements.size.toLong())
    }

    @Test
    fun changes_status_when_error_triggered() {
        val sensor = SingleCallSensor()
        sensor.triggerError(Sensor.AlertInfo("123", ALARM_DESCRIPTION))
        val measurements = sensor.measure()
        val measurement = getMeasurementFromList(measurements, "123")
        Assert.assertEquals("Measurement has error status", "ERROR", measurement.status)
    }

    @Test
    fun returns_measurement_with_description() {
        val sensor = SingleCallSensor()
        sensor.triggerError(Sensor.AlertInfo("123", ALARM_DESCRIPTION))
        val measurements = sensor.measure()
        val measurement = getMeasurementFromList(measurements, "123")
        Assert.assertEquals(ALARM_DESCRIPTION, measurement.description)
    }

    @Test
    fun only_one_measurement_for_key() {
        val sensor = SingleCallSensor()
        sensor.triggerError(Sensor.AlertInfo("123", ALARM_DESCRIPTION))
        sensor.triggerError(Sensor.AlertInfo("123", ALARM_DESCRIPTION))
        sensor.triggerOk(Sensor.AlertInfo("456", ALARM_DESCRIPTION))
        sensor.triggerOk(Sensor.AlertInfo("456", ALARM_DESCRIPTION))
        val measurements = sensor.measure()
        Assert.assertEquals(2, measurements.size.toLong())
    }

    @Test
    fun changes_status_back_to_ok() {
        val sensor = SingleCallSensor()
        sensor.triggerError(Sensor.AlertInfo("123", ALARM_DESCRIPTION))
        sensor.triggerOk(Sensor.AlertInfo("123", ALARM_DESCRIPTION))
        val measurements = sensor.measure()
        val measurement = getMeasurementFromList(measurements, "123")
        Assert.assertEquals("Measurement has info status", "INFO", measurement.status)
    }

    @Test
    fun changes_status_back_to_error() {
        val sensor = SingleCallSensor()
        sensor.triggerOk(Sensor.AlertInfo("123", ALARM_DESCRIPTION))
        sensor.triggerError(Sensor.AlertInfo("123", ALARM_DESCRIPTION))
        val measurements = sensor.measure()
        val measurement = getMeasurementFromList(measurements, "123")
        Assert.assertEquals("Measurement has error status", "ERROR", measurement.status)
    }

    private fun getMeasurementFromList(measurements: List<Measurement>, key: String): Measurement {
        return measurements.stream()
            .filter { it: Measurement -> key == it.key }
            .findFirst()
            .orElseThrow { IllegalStateException() }
    }
}
