package pro.panopticon.client.sensor.impl

import org.hamcrest.core.Is
import org.hamcrest.core.StringContains
import org.junit.Assert
import org.junit.Test
import pro.panopticon.client.model.Measurement
import pro.panopticon.client.sensor.Sensor
import pro.panopticon.client.util.NowSupplier
import java.time.LocalDateTime
import java.util.stream.IntStream

class SuccessrateSensorTest {
    @Test
    fun should_not_warn_before_reaching_enough_data() {
        val sensor = SuccessrateSensor(100, 0.1, 0.2)
        sensor.tickSuccess(Sensor.AlertInfo("key1", "description"))
        sensor.tickFailure(Sensor.AlertInfo("key1", "description"))
        val measurements = sensor.measure()
        Assert.assertThat(measurements.size, Is.`is`(1))
        val key1 = measurements.stream().filter { m: Measurement -> m.key == "key1" }.findAny()
        Assert.assertThat(key1.isPresent, Is.`is`(true))
        Assert.assertThat(key1.get().status, Is.`is`("INFO"))
        Assert.assertThat(key1.get().displayValue, StringContains.containsString("Last 2 calls: 1 success, 1 failure"))
        Assert.assertThat(key1.get().displayValue,
            StringContains.containsString("not enough calls to report status yet"))
    }

    @Test
    fun should_get_status_info() {
        val sensor = SuccessrateSensor(100, 0.1, 0.2)
        IntStream.range(0, 100).forEach { sensor.tickSuccess(Sensor.AlertInfo("key1", "description")) }
        val measurements = sensor.measure()
        Assert.assertThat(measurements.size, Is.`is`(1))
        val key1 = measurements.stream().filter { m: Measurement -> m.key == "key1" }.findAny()
        Assert.assertThat(key1.isPresent, Is.`is`(true))
        Assert.assertThat(key1.get().status, Is.`is`("INFO"))
        Assert.assertThat(key1.get().displayValue,
            StringContains.containsString("Last 100 calls: 100 success, 0 failure"))
    }

    @Test
    fun should_get_status_warn() {
        val sensor = SuccessrateSensor(100, 0.1, 0.2)
        IntStream.range(0, 90).forEach { sensor.tickSuccess(Sensor.AlertInfo("key1", "description")) }
        IntStream.range(0, 10).forEach { sensor.tickFailure(Sensor.AlertInfo("key1", "description")) }
        val measurements = sensor.measure()
        Assert.assertThat(measurements.size, Is.`is`(1))
        val key1 = measurements.stream().filter { m: Measurement -> m.key == "key1" }.findAny()
        Assert.assertThat(key1.isPresent, Is.`is`(true))
        Assert.assertThat(key1.get().status, Is.`is`("WARN"))
        Assert.assertThat(key1.get().displayValue,
            StringContains.containsString("Last 100 calls: 90 success, 10 failure"))
    }

    @Test
    fun should_get_status_error() {
        val sensor = SuccessrateSensor(100, 0.1, 0.2)
        IntStream.range(0, 80).forEach { sensor.tickSuccess(Sensor.AlertInfo("key1", "description")) }
        IntStream.range(0, 20).forEach { sensor.tickFailure(Sensor.AlertInfo("key1", "description")) }
        val measurements = sensor.measure()
        Assert.assertThat(measurements.size, Is.`is`(1))
        val key1 = measurements.stream().filter { m: Measurement -> m.key == "key1" }.findAny()
        Assert.assertThat(key1.isPresent, Is.`is`(true))
        Assert.assertThat(key1.get().status, Is.`is`("ERROR"))
        Assert.assertThat(key1.get().displayValue,
            StringContains.containsString("Last 100 calls: 80 success, 20 failure"))
    }

    @Test
    fun should_get_status_info_when_warn_levels_is_null() {
        val sensor = SuccessrateSensor(100, null, null)
        IntStream.range(0, 100).forEach { sensor.tickFailure(Sensor.AlertInfo("key1", "description")) }
        val measurements = sensor.measure()
        Assert.assertThat(measurements.size, Is.`is`(1))
        val key1 = measurements.stream().filter { m: Measurement -> m.key == "key1" }.findAny()
        Assert.assertThat(key1.isPresent, Is.`is`(true))
        Assert.assertThat(key1.get().status, Is.`is`("INFO"))
        Assert.assertThat(key1.get().displayValue,
            StringContains.containsString("Last 100 calls: 0 success, 100 failure"))
    }

    @Test
    fun should_only_keep_last_XXX_measurements() {
        val sensor = SuccessrateSensor(100, 0.1, 0.2)
        IntStream.range(0, 100).forEach { sensor.tickFailure(Sensor.AlertInfo("key1", "description")) }
        IntStream.range(0, 100).forEach { sensor.tickSuccess(Sensor.AlertInfo("key1", "description")) }
        val measurements = sensor.measure()
        Assert.assertThat(measurements.size, Is.`is`(1))
        val key1 = measurements.stream().filter { m: Measurement -> m.key == "key1" }.findAny()
        Assert.assertThat(key1.isPresent, Is.`is`(true))
        Assert.assertThat(key1.get().status, Is.`is`("INFO"))
        Assert.assertThat(key1.get().displayValue,
            StringContains.containsString("Last 100 calls: 100 success, 0 failure"))
    }

    @Test
    fun should_keep_data_for_multiple_keys() {
        val sensor = SuccessrateSensor(100, 0.1, 0.2)
        IntStream.range(0, 50).forEach { sensor.tickSuccess(Sensor.AlertInfo("key1", "description")) }
        IntStream.range(0, 50).forEach { sensor.tickFailure(Sensor.AlertInfo("key1", "description")) }
        IntStream.range(0, 98).forEach { sensor.tickSuccess(Sensor.AlertInfo("key2", "description")) }
        IntStream.range(0, 2).forEach { sensor.tickFailure(Sensor.AlertInfo("key2", "description")) }
        val measurements = sensor.measure()
        Assert.assertThat(measurements.size, Is.`is`(2))
        val key1 = measurements.stream().filter { m: Measurement -> m.key == "key1" }.findAny()
        Assert.assertThat(key1.isPresent, Is.`is`(true))
        Assert.assertThat(key1.get().status, Is.`is`("ERROR"))
        Assert.assertThat(key1.get().displayValue,
            StringContains.containsString("Last 100 calls: 50 success, 50 failure"))
        val key2 = measurements.stream().filter { m: Measurement -> m.key == "key2" }.findAny()
        Assert.assertThat(key2.isPresent, Is.`is`(true))
        Assert.assertThat(key2.get().status, Is.`is`("INFO"))
        Assert.assertThat(key2.get().displayValue,
            StringContains.containsString("Last 100 calls: 98 success, 2 failure"))
    }

    @Test
    fun should_display_description_when_present() {
        val sensor = SuccessrateSensor(100, 0.1, 0.2)
        IntStream.range(0, 50).forEach { sensor.tickSuccess(Sensor.AlertInfo("key1", "description")) }
        IntStream.range(0, 50).forEach { sensor.tickFailure(Sensor.AlertInfo("key1", "description")) }
        val measurements = sensor.measure()
        val key1 = measurements.stream().filter { m: Measurement -> m.key == "key1" }.findAny()
        Assert.assertThat(key1.get().description, Is.`is`("description"))
    }

    @Test
    fun should_ignore_errors_if_all_ticks_are_outdated() {
        val nowSupplier = NowSupplierMock()
        val sensor = SuccessrateSensor(100, 0.1, 0.2, nowSupplier)
        nowSupplier.mockThePast = true
        IntStream.range(0, 50).forEach { sensor.tickFailure(Sensor.AlertInfo("key1", "description")) }
        nowSupplier.mockThePast = false
        IntStream.range(0, 50).forEach { sensor.tickSuccess(Sensor.AlertInfo("key1", "description")) }
        val measurements = sensor.measure()
        val key1 = measurements.stream().filter { m: Measurement -> m.key == "key1" }.findAny()
        Assert.assertThat(key1.get().status, Is.`is`("INFO"))
    }

    internal inner class NowSupplierMock : NowSupplier {
        var mockThePast = false
        override fun now(): LocalDateTime {
            return if (mockThePast) {
                LocalDateTime.now()
                    .minusHours((SuccessrateSensor.HOURS_FOR_ERROR_TICK_TO_BE_CONSIDERED_OUTDATED + 4).toLong())
            } else {
                LocalDateTime.now()
            }
        }
    }
}
