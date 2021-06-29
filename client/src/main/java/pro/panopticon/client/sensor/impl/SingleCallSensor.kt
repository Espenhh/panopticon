package pro.panopticon.client.sensor.impl

import pro.panopticon.client.model.Measurement
import pro.panopticon.client.model.Measurement.Status
import pro.panopticon.client.sensor.Sensor
import java.util.HashMap

open class SingleCallSensor : Sensor {
    private val measurements: MutableMap<Sensor.AlertInfo, Status>
    override fun measure(): List<Measurement> {
        return measurements.entries.map {
            Measurement(
                key = it.key.sensorKey,
                status = it.value,
                displayValue = getDisplayValue(it.value),
                description = it.key.description,
            )
        }
    }

    open fun triggerError(alertInfo: Sensor.AlertInfo) {
        measurements.compute(alertInfo) { _: Sensor.AlertInfo?, _: Status? -> Status.ERROR }
    }

    open fun triggerWarn(alertInfo: Sensor.AlertInfo) {
        measurements.compute(alertInfo) { _: Sensor.AlertInfo?, _: Status? -> Status.WARN }
    }

    open fun triggerOk(alertInfo: Sensor.AlertInfo) {
        measurements.compute(alertInfo) { _: Sensor.AlertInfo?, _: Status? -> Status.INFO }
    }

    private fun getDisplayValue(status: Status): String {
        return when (status) {
            Status.ERROR -> "In error"
            Status.WARN -> "Status: WARN"
            Status.INFO -> "Status: OK"
        }
    }

    init {
        measurements = HashMap()
    }
}
