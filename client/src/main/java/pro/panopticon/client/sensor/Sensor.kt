package pro.panopticon.client.sensor

import pro.panopticon.client.model.Measurement

fun interface Sensor {
    fun measure(): List<Measurement>

    data class AlertInfo(
        /**
         * Key used to separate alerts from each other.
         * Example:
         * "entur.rest.calls"
         */
        val sensorKey: String,
        /**
         * A human / guard-friendly description of what is happening and which actions that needs to be taken.
         *
         * Example:
         * "When this alert is triggered, the critical Feature X is not working properly. You should contact Company Y."
         */
        val description: String,
    )
}
