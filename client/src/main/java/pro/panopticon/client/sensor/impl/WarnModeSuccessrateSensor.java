package pro.panopticon.client.sensor.impl;

import pro.panopticon.client.util.NowSupplier;

/**
 * This sensor is only capable of giving warnings, and will never result in an alert to pagerduty
 */
public class WarnModeSuccessrateSensor extends SuccessrateSensor {
    public WarnModeSuccessrateSensor(int numberToKeep, Double warnLimit) {
        super(numberToKeep, warnLimit, 1.1);
    }

    WarnModeSuccessrateSensor(int numberToKeep, Double warnLimit, NowSupplier nowSupplier) {
        super(numberToKeep, warnLimit, 1.1, nowSupplier);
    }
}
