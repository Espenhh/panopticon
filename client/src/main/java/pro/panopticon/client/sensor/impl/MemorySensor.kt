package pro.panopticon.client.sensor.impl

import com.amazonaws.services.cloudwatch.model.StandardUnit
import pro.panopticon.client.model.Measurement
import pro.panopticon.client.model.MetricDimension.Companion.instanceDimension
import pro.panopticon.client.sensor.Sensor
import pro.panopticon.client.util.SystemStatus

class MemorySensor @JvmOverloads constructor(
    private val warnLimitNow: Int = 85,
    private val errorLimitNow: Int = 95,
    private val warnLimitHeap: Int = 75,
    private val errorLimitHeap: Int = 95,
    private val hostname: String? = null,
) : Sensor {

    override fun measure(): List<Measurement> {
        return SystemStatus().let {
            listOfNotNull(
                createMeasurement("mem.heap.now", it.heapUsed(), it.heapMax(), warnLimitNow, errorLimitNow),
                createMeasurement("mem.heap.lastGC", it.heapAfterGC(), it.heapMax(), warnLimitHeap, errorLimitHeap)
            )
        }
    }

    private fun createMeasurement(
        key: String,
        used: Long,
        max: Long,
        warnLimit: Int,
        errorLimit: Int,
    ): Measurement? {
        if (max == 0L || used == -1L) {
            return null
        }
        val percentUsed = used / (max / 100)
        val displayValue = toMB(used).toString() + " of " + toMB(max) + " MB (" + percentUsed + "%)"
        val dimensions = hostname?.let { instanceDimension(it) }
                             ?.let { listOf(it) }
                         ?: emptyList()

        return Measurement(
            key = key,
            status = status(percentUsed, warnLimit, errorLimit),
            displayValue = displayValue,
            cloudwatchValue = Measurement.CloudwatchValue(
                percentUsed.toDouble(),
                StandardUnit.Percent,
                dimensions,
            ),
            description = DESCRIPTION,
        )
    }

    private fun status(percentUsed: Long, warnLimit: Int, errorLimit: Int): String {
        return when {
            percentUsed > errorLimit -> "ERROR"
            percentUsed > warnLimit -> "WARN"
            else -> "INFO"
        }
    }

    private fun toMB(bytes: Long): Long {
        return bytes / BYTES_IN_MB
    }

    companion object {
        private const val DESCRIPTION =
            "When this alarm is triggered, you should check the memory status of the other nodes as well. " +
            "There might be a memory leak somewhere in the application triggering this, so a restart will buy you some time"
        private const val BYTES_IN_MB = (1024 * 1024).toLong()
    }
}
