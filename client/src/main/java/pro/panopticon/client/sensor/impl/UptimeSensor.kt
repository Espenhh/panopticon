package pro.panopticon.client.sensor.impl

import pro.panopticon.client.model.Measurement
import pro.panopticon.client.sensor.Sensor
import java.time.LocalDateTime
import java.util.ArrayList

class UptimeSensor : Sensor {
    override fun measure(): List<Measurement> {
        val measurements: MutableList<Measurement> = ArrayList()
        measurements.add(Measurement("uptime.since", Measurement.Status.INFO, STARTED.toString(), DESCRIPTION))
        return measurements
    }

    companion object {
        private const val DESCRIPTION = ""
        private val STARTED = LocalDateTime.now()
    }
}
