package pro.panopticon.client.sensor.impl

import pro.panopticon.client.model.Measurement
import pro.panopticon.client.sensor.Sensor
import java.util.ArrayList

class JavaVersionSensor : Sensor {
    private val version = System.getProperty("java.version")
    override fun measure(): List<Measurement> {
        return listOf(
            Measurement(
                key = "java.version",
                status = "INFO",
                displayValue = version,
                description = ""
            )
        )
    }
}
