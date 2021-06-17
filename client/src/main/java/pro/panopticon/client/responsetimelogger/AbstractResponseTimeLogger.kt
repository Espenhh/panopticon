package pro.panopticon.client.responsetimelogger

import com.amazonaws.services.cloudwatch.model.StandardUnit
import pro.panopticon.client.awscloudwatch.CloudwatchClient
import pro.panopticon.client.awscloudwatch.HasCloudwatchConfig
import pro.panopticon.client.model.Measurement
import pro.panopticon.client.sensor.Sensor
import java.util.Date
import java.util.Vector

abstract class AbstractResponseTimeLogger(
    hasCloudwatchConfig: HasCloudwatchConfig?,
    private val cloudwatchClient: CloudwatchClient?,
) : Sensor {
    private val namespace: String =
        String.format("responsetimes-%s-%s", hasCloudwatchConfig?.appName, hasCloudwatchConfig?.environment)

    // Using Vector since it's syncronized, e.g thread safe
    private var counts = Vector<CloudwatchClient.CloudwatchStatistic>()
    fun addResponseTimeMeasurement(name: String?, timeInMilliseconds: Long) {
        counts.add(CloudwatchClient.CloudwatchStatistic(name!!,
            timeInMilliseconds.toDouble(),
            StandardUnit.Milliseconds,
            Date()))
    }

    override fun measure(): List<Measurement> {
        val toSubmit: List<CloudwatchClient.CloudwatchStatistic> = counts
        counts = Vector()
        cloudwatchClient?.sendStatistics(namespace, toSubmit)

        // This sensor returns no measurements for now – it's just implemented as a sensor for similarity to the other concepts
        return emptyList()
    }
}
