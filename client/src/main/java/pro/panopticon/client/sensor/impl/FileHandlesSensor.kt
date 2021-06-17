package pro.panopticon.client.sensor.impl

import com.amazonaws.services.cloudwatch.model.StandardUnit
import pro.panopticon.client.model.Measurement
import pro.panopticon.client.sensor.Sensor
import pro.panopticon.client.util.SystemStatus

class FileHandlesSensor(private val warnAfter: Long, private val errorAfter: Long) : Sensor {
    override fun measure(): List<Measurement> {
        val s = SystemStatus()
        val open = s.openFileHandles()
        val max = s.maxFileHandles()
        val percent = open.toDouble() / max.toDouble() * 100
        val displayValue = String.format("%s of %s filehandles used (%.2f%%)", open, max, percent)

        return listOf(
            Measurement(
                key = "filehandles",
                status = statusFromOpenFileHandles(open),
                displayValue = displayValue,
                cloudwatchValue = Measurement.CloudwatchValue(percent, StandardUnit.Percent),
                description = ""
            )
        )
    }

    private fun statusFromOpenFileHandles(open: Long): String {
        return when {
            open >= errorAfter -> "ERROR"
            open >= warnAfter -> "WARN"
            else -> "INFO"
        }
    }
}
