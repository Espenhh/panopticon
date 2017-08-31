package pro.panopticon.client.sensor.impl;

import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.sensor.Sensor;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.List;

public abstract class CachedSensor implements Sensor {

    private TemporalAmount cacheTime;

    private LocalDateTime lastFetchTime;
    private List<Measurement> cachedValue;

    public CachedSensor(TemporalAmount cacheTime) {
        this.cacheTime = cacheTime;
    }

    public abstract List<Measurement> calculateMeasurementsForCaching();

    @Override
    public List<Measurement> measure() {
        if (lastFetchTime == null || cachedValue == null || lastFetchTime.isBefore(LocalDateTime.now().minus(cacheTime))) {
            cachedValue = calculateMeasurementsForCaching();
            lastFetchTime = LocalDateTime.now();
        }
        return cachedValue;
    }
}
