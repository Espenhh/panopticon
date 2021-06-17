package pro.panopticon.client.sensor.impl

import pro.panopticon.client.model.Measurement
import pro.panopticon.client.sensor.Sensor
import java.util.HashMap

open class SingleCallSensor : Sensor {
    private val measurements: MutableMap<Sensor.AlertInfo, Status>
    override fun measure(): List<Measurement> {
        return measurements.entries.map {
            Measurement(
                it.key.sensorKey,
                getPanopticonStatus(it.value),
                getDisplayValue(it.value),
                it.key.description
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
        measurements.compute(alertInfo) { _: Sensor.AlertInfo?, _: Status? -> Status.OK }
    }

    private fun getPanopticonStatus(status: Status): String {
        return when (status) {
            Status.ERROR -> "ERROR"
            Status.WARN -> "WARN"
            Status.OK -> "INFO"
        }
    }

    private fun getDisplayValue(status: Status): String {
        return when (status) {
            Status.ERROR -> "In error"
            Status.WARN -> "Status: WARN"
            Status.OK -> "Status: OK"
        }
    }

    private enum class Status {
        OK, WARN, ERROR
    }

    init {
        measurements = HashMap()
    }
}
