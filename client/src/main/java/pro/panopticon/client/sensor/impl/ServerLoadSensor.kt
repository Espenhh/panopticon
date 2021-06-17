package pro.panopticon.client.sensor.impl

import com.amazonaws.services.cloudwatch.model.StandardUnit
import pro.panopticon.client.model.Measurement
import pro.panopticon.client.sensor.Sensor
import pro.panopticon.client.util.SystemStatus
import java.text.DecimalFormat

class ServerLoadSensor : Sensor {
    override fun measure(): List<Measurement> {
        val s = SystemStatus()
        val load = s.load()
        val formatted = DecimalFormat("#.##").format(load)
        val status = when {
            load > 10 -> "ERROR"
            load > 5 -> "WARN"
            else -> "INFO"
        }

        return listOf(
            Measurement(
                key = "load.avg",
                status = status,
                displayValue = formatted,
                cloudwatchValue = Measurement.CloudwatchValue(load, StandardUnit.None),
                description = ""
            )
        )
    }
}
