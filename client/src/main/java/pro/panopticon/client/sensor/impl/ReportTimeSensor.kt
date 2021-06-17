package pro.panopticon.client.sensor.impl

import pro.panopticon.client.model.Measurement
import pro.panopticon.client.sensor.Sensor
import java.time.LocalDateTime

class ReportTimeSensor : Sensor {
    override fun measure(): List<Measurement> {
        return listOf(
            Measurement(
                key = "report.generated",
                status = "INFO",
                displayValue = LocalDateTime.now().toString(),
                description = "",
            )
        )
    }
}
