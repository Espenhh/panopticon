package pro.panopticon.client.sensor.impl

/**
 * This sensor is only capable of giving warnings, and will never result in an alert to pagerduty
 */
open class WarnModeSuccessrateSensor(numberToKeep: Int, warnLimit: Double) :
    SuccessrateSensor(numberToKeep, warnLimit, 1.1)
