package pro.panopticon.client.sensor.impl

import pro.panopticon.client.model.Measurement
import pro.panopticon.client.model.Measurement.Status
import pro.panopticon.client.sensor.Sensor
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

open class ValueChainSensor : Sensor {
    private val sensors = ConcurrentHashMap<ValueChainInfo, SensorData>()

    fun create(
        key: String,
        description: String,
        expectedCompletions: Int = 1,
        period: Duration = Duration.ofMinutes(10),
        gracePeriod: Duration = Duration.ofMinutes(10),
        disabledHours: DisabledHours = DisabledHours(23, 6),
    ): ValueChainCompleter {
        val info = ValueChainInfo(key, description, expectedCompletions, period, gracePeriod, disabledHours)
        sensors[info] = SensorData(
            created = ZonedDateTime.now(),
            completions = emptyList()
        )
        return ValueChainCompleter { complete(info, it) }
    }

    private fun complete(info: ValueChainInfo, completionTime: ZonedDateTime) {
        sensors.compute(info) { _, data ->
            data?.completions.orEmpty()
                .plus(completionTime)
                .takeLast(info.expectedCompletions)
                .let {
                    SensorData(
                        created = data?.created ?: ZonedDateTime.now(),
                        completions = it,
                    )
                }
        }
    }

    override fun measure(): List<Measurement> {
        return sensors.map { toMeasurement(it.key, it.value) }
    }

    private fun toMeasurement(info: ValueChainInfo, data: SensorData): Measurement {
        val status = getStatus(info, data)

        val displayValue = when (status) {
            Status.INFO -> {
                val completions = data.getCompletionCountAfter(info.getEarliestCompletionTime())
                "Minst ${completions} fullføringer siste ${getDurationText(info.period)}"
            }
            Status.WARN -> {
                val completions = data.getCompletionCountAfter(info.getEarliestCompletionTime())
                "$completions fullføringer siste ${getDurationText(info.period)}. Forventet minst ${info.expectedCompletions}."
            }
            Status.ERROR -> {
                val lastCompletionText = data.completions.lastOrNull()?.format(DateTimeFormatter.ISO_DATE_TIME)
                                             ?.let { "Siste registrerte fullføring: $it" }
                                         ?: "Ingen registrerte fullføringer siden oppstart."
                val completions = data.getCompletionCountAfter(info.getEarliestCompletionTimeIncludingGracePeriod())
                "$completions fullføringer siste ${getDurationText(info.period.plus(info.gracePeriod))}. " +
                "Forventet minst ${info.expectedCompletions}. $lastCompletionText"
            }
        }

        return Measurement(
            key = info.key,
            status = status,
            displayValue = displayValue,
            description = info.description,
        )
    }

    private fun getDurationText(duration: Duration): String {
        return when {
            duration.toHours() == 1L -> "timen"
            duration.toHours() > 1 -> "${duration.toHours()} timer"
            duration.toMinutes() == 1L -> "minuttet"
            duration.toMinutes() > 1 -> "${duration.toMinutes()} minuttene"
            else -> "${duration.toSeconds()} sekundene"
        }
    }

    private fun getStatus(info: ValueChainInfo, data: SensorData): Status {
        val now = ZonedDateTime.now()
        val startupPeriod = data.created.plus(info.period)
        val startupGracePeriod = data.created.plus(info.period).plus(info.gracePeriod)

        return when {
            info.disabledHours.isInsideDeadPeriod() -> Status.INFO
            data.completions.isEmpty() && now.isAfter(startupGracePeriod) -> Status.ERROR
            data.completions.isEmpty() && now.isAfter(startupPeriod) -> Status.WARN
            data.completions.all { it.isAfter(info.getEarliestCompletionTime()) } -> Status.INFO
            data.completions.all { it.isAfter(info.getEarliestCompletionTimeIncludingGracePeriod()) } -> Status.WARN
            else -> Status.ERROR
        }
    }

    data class ValueChainCompleter(
        private val completeCallback: (withTime: ZonedDateTime) -> Unit,
    ) {
        fun complete(withTime: ZonedDateTime = ZonedDateTime.now()) {
            completeCallback(withTime)
        }
    }

    private data class SensorData(
        val created: ZonedDateTime,
        val completions: List<ZonedDateTime>,
    ) {
        fun getCompletionCountAfter(time: ZonedDateTime): Int {
            return completions.filter { it.isAfter(time) }.count()
        }
    }

    private data class ValueChainInfo(
        val key: String,
        val description: String,
        val expectedCompletions: Int = 1,
        val period: Duration = Duration.ofMinutes(10),
        val gracePeriod: Duration = Duration.ofMinutes(10),
        val disabledHours: DisabledHours = DisabledHours(23, 6),
    ) {
        fun getEarliestCompletionTime(): ZonedDateTime {
            return ZonedDateTime.now().minus(period)
        }

        fun getEarliestCompletionTimeIncludingGracePeriod(): ZonedDateTime {
            return getEarliestCompletionTime().minus(gracePeriod)
        }
    }

    data class DisabledHours(
        val startHour: Int,
        val endHour: Int,
    ) {
        fun isInsideDeadPeriod(time: ZonedDateTime = ZonedDateTime.now()): Boolean {
            val hour = time.hour
            return when {
                hour == startHour -> true
                hour == endHour -> false
                hour in startHour..endHour -> true
                startHour > endHour && hour !in endHour..startHour -> true
                else -> false
            }
        }
    }
}

