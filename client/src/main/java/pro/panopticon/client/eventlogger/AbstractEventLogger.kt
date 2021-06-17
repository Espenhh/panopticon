package pro.panopticon.client.eventlogger

import org.slf4j.LoggerFactory
import pro.panopticon.client.awscloudwatch.CloudwatchClient
import pro.panopticon.client.awscloudwatch.HasCloudwatchConfig
import pro.panopticon.client.model.Measurement
import pro.panopticon.client.model.MetricDimension
import pro.panopticon.client.sensor.Sensor
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Vector
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.DoubleAdder

abstract class AbstractEventLogger(hasCloudwatchConfig: HasCloudwatchConfig, cloudwatchClient: CloudwatchClient?) :
    Sensor {
    private val LOG = LoggerFactory.getLogger(this.javaClass)
    private val namespace: String
    private var counts: ConcurrentMap<String, DoubleAdder> = ConcurrentHashMap()
    private var ticksWithDimensions = Vector<CloudwatchClient.CloudwatchStatistic>()
    private val hasCloudwatchConfig: HasCloudwatchConfig?
    private val cloudwatchClient: CloudwatchClient?

    open fun tickAndLog(event: HasEventInfo, vararg logappends: String) {
        tickAndLog(event, 1.0, *logappends)
    }

    open fun tickAndLogException(event: HasEventInfo, e: Exception) {
        tickAndLog(event, 1.0, stackTraceToString(e))
    }

    private fun stackTraceToString(e: Exception): String {
        val stringWriter = StringWriter()
        e.printStackTrace(PrintWriter(stringWriter))
        return stringWriter.toString()
    }

    open fun tickAndLog(event: HasEventInfo, count: Double, vararg logappends: String) {
        performLog(event, *logappends)
        performTick(event, count)
    }

    open fun tick(event: HasEventInfo) {
        performTick(event, 1.0)
    }

    open fun tick(event: HasEventInfo, count: Double = 1.0) {
        performTick(event, count)
    }

    open fun tick(event: HasEventInfo, vararg dimensions: MetricDimension) {
        val statistics = CloudwatchClient.CloudwatchStatistic(
            event.eventName,
            1.0
        ).withDimensions(dimensions.toList())
        ticksWithDimensions.add(statistics)
    }

    open fun tick(event: HasEventInfo, count: Double, vararg dimensions: MetricDimension) {
        val statistics = CloudwatchClient.CloudwatchStatistic(
            event.eventName,
            count
        ).withDimensions(dimensions.toList())
        ticksWithDimensions.add(statistics)
    }

    private fun performLog(event: HasEventInfo, vararg logappends: String) {
        LOG.info("AUDIT EVENT - [" + event.eventType + "] - [" + event.eventName + "] - " +
                 logappends.joinToString(" - ") { s: String -> "[$s]" })
    }

    private fun performTick(event: HasEventInfo, count: Double) {
        counts.computeIfAbsent(event.eventName) { DoubleAdder() }.add(count)
    }

    override fun measure(): List<Measurement> {
        val countsToProcess = counts
        val countsWithDimensionToProcess = ticksWithDimensions
        resetTicks()
        if (statisticsEnabled()) {
            val statistics = createCountStatistics(countsToProcess)
            statistics.addAll(countsWithDimensionToProcess)
            cloudwatchClient!!.sendStatistics(namespace, statistics)
        }
        return countsToProcess.entries
            .map { (key, value) ->
                Measurement(
                    key = "audit.$key",
                    status = "INFO",
                    displayValue = "Last minute: ${value.toDouble()}",
                    description = ""
                )
            }
            .toList()
    }

    private fun statisticsEnabled(): Boolean {
        return cloudwatchClient != null && hasCloudwatchConfig != null && hasCloudwatchConfig.auditeventStatisticsEnabled()
    }

    private fun resetTicks() {
        counts = ConcurrentHashMap()
        ticksWithDimensions = Vector()
    }

    private fun createCountStatistics(countsToProcess: ConcurrentMap<String, DoubleAdder>): MutableList<CloudwatchClient.CloudwatchStatistic> {
        return countsToProcess.entries
            .map { e: Map.Entry<String, DoubleAdder> ->
                CloudwatchClient.CloudwatchStatistic(e.key,
                    e.value.toDouble())
            }
            .toMutableList()
    }

    init {
        this.hasCloudwatchConfig = hasCloudwatchConfig
        this.cloudwatchClient = cloudwatchClient
        namespace = "audit-${hasCloudwatchConfig.appName}-${hasCloudwatchConfig.environment}"
    }
}
