package pro.panopticon.client.sensor.impl

import pro.panopticon.client.model.Measurement
import pro.panopticon.client.sensor.Sensor
import java.time.LocalDateTime
import java.time.temporal.TemporalAmount

abstract class CachedSensor(private val cacheTime: TemporalAmount) : Sensor {
    private var lastFetchTime: LocalDateTime? = null
    private var cachedValue: List<Measurement>? = null

    abstract fun calculateMeasurementsForCaching(): List<Measurement>
    override fun measure(): List<Measurement> {
        if (lastFetchTime == null
            || cachedValue == null
            || lastFetchTime?.isBefore(LocalDateTime.now().minus(cacheTime)) == true
        ) {
            cachedValue = calculateMeasurementsForCaching()
            lastFetchTime = LocalDateTime.now()
        }
        return cachedValue ?: emptyList()
    }
}
