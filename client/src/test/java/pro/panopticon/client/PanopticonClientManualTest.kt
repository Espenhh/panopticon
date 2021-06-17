package pro.panopticon.client

import org.junit.Ignore
import org.junit.Test
import pro.panopticon.client.model.ComponentInfo
import pro.panopticon.client.model.Measurement
import pro.panopticon.client.model.Status
import pro.panopticon.client.sensor.Sensor
import java.util.ArrayList
import java.util.Arrays

@Ignore("This is a manual test. Run it from the IDE to test against production :)")
class PanopticonClientManualTest {
    @Test
    fun test_add_component_status() {
        val measurements = Arrays.asList(
            Measurement("jetty.threads", "INFO", "100 av 768 (14%)", ""),
            Measurement("memory.usage", "WARN", "200MB av 560MB (40%)", "")
        )
        val status = Status("prod", "Mobile system", "mobile-backend", "server123", measurements)
        CLIENT.sendMeasurementsToPanopticon(status)
    }

    @Test
    @Throws(InterruptedException::class)
    fun test_run_scheduled_task() {
        val componentInfo = ComponentInfo("prod", "Mobile system", "mobile-backend", "server123")
        val sensors = Arrays.asList(
            mockSystemSensor(),
            mockJettySensor()
        )
        CLIENT.startScheduledStatusUpdate(componentInfo, sensors)
        Thread.sleep((1000 * 60 * 10).toLong())
    }

    private fun mockSystemSensor(): Sensor {
        return Sensor {
            val measurements: MutableList<Measurement> = ArrayList()
            measurements.add(Measurement("system.load", "INFO", "1.23", ""))
            measurements.add(Measurement("memory.usage", "WARN", "700 av 1024 MB (78%)", ""))
            measurements
        }
    }

    private fun mockJettySensor(): Sensor {
        return Sensor {
            val measurements: MutableList<Measurement> = ArrayList()
            measurements.add(Measurement("jetty.threads", "INFO", "100 av 768 (14%)", ""))
            measurements
        }
    }

    companion object {
        private val CLIENT = PanopticonClient("http://example.com/domainhere", null, null)
    }
}
