package pro.panopticon.client.sensor.impl

import com.amazonaws.services.cloudwatch.model.StandardUnit
import pro.panopticon.client.model.Measurement
import pro.panopticon.client.sensor.Sensor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadPoolExecutor

class ThreadPoolSensor : Sensor {
    private val threadPools = ConcurrentHashMap<String, ThreadPoolExecutor>()

    fun register(key: String, threadPool: ThreadPoolExecutor) {
        threadPools[key] = threadPool
    }

    override fun measure(): List<Measurement> {
        return threadPools.entries.flatMap { (key, threadPool) ->
            mutableListOf(
                createMeasurement("$key.threadpool.poolsize", threadPool.poolSize),
                createMeasurement("$key.threadpool.largestpoolsize", threadPool.largestPoolSize),
                createMeasurement("$key.threadpool.activecount", threadPool.activeCount),
                createMeasurement("$key.threadpool.taskcount", threadPool.taskCount),
                createMeasurement("$key.threadpool.completedcount", threadPool.completedTaskCount),
            )
        }
    }

    private fun createMeasurement(key: String, value: Number): Measurement {
        return Measurement(
            key = key,
            status = Measurement.Status.INFO,
            displayValue = value.toString(),
            cloudwatchValue = Measurement.CloudwatchValue(value.toDouble(), StandardUnit.Count),
            description = ""
        )
    }
}
