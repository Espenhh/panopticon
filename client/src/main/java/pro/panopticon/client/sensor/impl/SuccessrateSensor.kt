package pro.panopticon.client.sensor.impl

import com.amazonaws.services.cloudwatch.model.StandardUnit
import mu.KotlinLogging
import org.apache.commons.collections4.queue.CircularFifoQueue
import pro.panopticon.client.model.Measurement
import pro.panopticon.client.sensor.Sensor
import pro.panopticon.client.util.NowSupplier
import pro.panopticon.client.util.NowSupplierImpl
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.min

open class SuccessrateSensor : Sensor {
    private val LOG = KotlinLogging.logger { }

    /**
     * Number of events to keep.
     */
    private val numberToKeep: Int

    /**
     * Triggers an alert to Slack when reached.
     * Should always be a Double between 0.00 and 1.00
     * Format: percentage / 100
     *
     *
     * Example: 0.1 will trigger a warning at 10% failure rate
     */
    private val warnLimit: Double?

    /**
     * Triggers an alert to Slack and PagerDuty when reached.
     * Should always be a Double between 0.00 and 1.00
     * Format: percentage / 100
     *
     *
     * Example: 0.2 will trigger an alert at 20% failure rate
     */
    private val errorLimit: Double?
    private val eventQueues: MutableMap<Sensor.AlertInfo, CircularFifoQueue<Tick>> = mutableMapOf()
    private val previousErrorPercentage = mutableMapOf<Sensor.AlertInfo, Double>()
    private val nowSupplier: NowSupplier

    constructor(numberToKeep: Int, warnLimit: Double?, errorLimit: Double?) {
        this.numberToKeep = numberToKeep
        this.warnLimit = warnLimit
        this.errorLimit = errorLimit
        nowSupplier = NowSupplierImpl()
    }

    internal constructor(numberToKeep: Int, warnLimit: Double?, errorLimit: Double?, nowSupplier: NowSupplier) {
        this.numberToKeep = numberToKeep
        this.warnLimit = warnLimit
        this.errorLimit = errorLimit
        this.nowSupplier = nowSupplier
    }

    @Synchronized
    fun tickFailure(alertInfo: Sensor.AlertInfo) {
        try {
            getQueueForKey(alertInfo).add(Tick(Event.FAILURE, nowSupplier.now()))
        } catch (e: Exception) {
            LOG.warn("Something went wrong when counting FAILURE for " + alertInfo.sensorKey, e)
        }
    }

    @Synchronized
    fun tickAndLogFailure(alertInfo: Sensor.AlertInfo, vararg logAppends: String?) {
        tickFailure(alertInfo)
        LOG.info("SENSOR SUCCESS RATE - [FAILURE] - [" + alertInfo.sensorKey + "] - [" + alertInfo.description + "] - " + logAppends.joinToString(
            " - ") { "[$it]" })
    }

    @Synchronized
    fun tickSuccess(alertInfo: Sensor.AlertInfo) {
        try {
            getQueueForKey(alertInfo).add(Tick(Event.SUCCESS, nowSupplier.now()))
        } catch (e: Exception) {
            LOG.warn("Something went wrong when counting SUCCESS for " + alertInfo.sensorKey, e)
        }
    }

    @Synchronized
    fun tickAndLogSuccess(alertInfo: Sensor.AlertInfo, vararg logAppends: String?) {
        tickSuccess(alertInfo)
        LOG.info("SENSOR SUCCESS RATE - [SUCCESS] - [" + alertInfo.sensorKey + "] - [" + alertInfo.description + "] - " + logAppends.joinToString(
            " - ") { "[$it]" })
    }

    private fun getQueueForKey(key: Sensor.AlertInfo): CircularFifoQueue<Tick> {
        return eventQueues.computeIfAbsent(key) { CircularFifoQueue(numberToKeep) }
    }

    override fun measure(): List<Measurement> {
        return eventQueues.entries
            .map { this.measure(it.key, it.value) }
    }

    private fun measure(alertInfo: Sensor.AlertInfo, ticks: CircularFifoQueue<Tick>): Measurement {
        val all = ticks.size
        val success = ticks.count { tick: Tick -> tick.event == Event.SUCCESS }
        val failure = ticks.count { tick: Tick -> tick.event == Event.FAILURE }
        val previousFailureDouble = previousErrorPercentage[alertInfo] ?: 0.0
        val percentFailureDouble: Double = (if (all > 0) failure.toDouble() / all.toDouble() else 0.0)
            .also { previousErrorPercentage[alertInfo] = it }
        val enoughDataToAlert = all == numberToKeep
        val hasRecentErrorTicks = hasRecentErrorTicks(ticks)
        val display = String.format("Last %s calls: %s success, %s failure (%.2f%% failure)%s%s",
            min(all, numberToKeep),
            success,
            all - success,
            percentFailureDouble * 100,
            if (enoughDataToAlert) "" else " - not enough calls to report status yet",
            if (hasRecentErrorTicks) "" else " - no recent error ticks",
            when {
                previousFailureDouble == percentFailureDouble -> ""
                previousFailureDouble < percentFailureDouble -> " :chart_with_upwards_trend:"
                else -> " :chart_with_downwards_trend:"
            }
        )
        val status = decideStatus(enoughDataToAlert, percentFailureDouble, hasRecentErrorTicks)
        return Measurement(
            key = alertInfo.sensorKey,
            status = status,
            cloudwatchValue = Measurement.CloudwatchValue(percentFailureDouble * 100, StandardUnit.Percent),
            displayValue = display,
            description = alertInfo.description,
        )
    }

    private fun decideStatus(
        enoughDataToAlert: Boolean,
        percentFailure: Double,
        hasRecentErrorTicks: Boolean,
    ): Measurement.Status {
        return when {
            !enoughDataToAlert -> Measurement.Status.INFO
            !hasRecentErrorTicks -> Measurement.Status.INFO
            (errorLimit != null && percentFailure >= errorLimit) -> Measurement.Status.ERROR
            (warnLimit != null && percentFailure >= warnLimit) -> Measurement.Status.WARN
            else -> Measurement.Status.INFO
        }
    }

    private fun hasRecentErrorTicks(ticks: CircularFifoQueue<Tick>): Boolean {
        return ticks
            .filter { tick: Tick -> tick.event == Event.FAILURE }
            .any { tick: Tick ->
                ChronoUnit.HOURS.between(tick.createdAt,
                    nowSupplier.now()) < HOURS_FOR_ERROR_TICK_TO_BE_CONSIDERED_OUTDATED
            }
    }

    private enum class Event {
        SUCCESS, FAILURE
    }

    private class Tick(val event: Event, val createdAt: LocalDateTime)
    companion object {
        const val HOURS_FOR_ERROR_TICK_TO_BE_CONSIDERED_OUTDATED = 1
    }
}
