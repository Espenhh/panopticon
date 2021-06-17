package pro.panopticon.client.sensor.impl

import pro.panopticon.client.util.NowSupplier

/**
 * This sensor is only capable of giving warnings, and will never result in an alert to pagerduty
 */
open class WarnModeSuccessrateSensor : SuccessrateSensor {
    constructor(numberToKeep: Int, warnLimit: Double?) : super(numberToKeep, warnLimit, 1.1)
    internal constructor(numberToKeep: Int, warnLimit: Double?, nowSupplier: NowSupplier?) : super(numberToKeep,
        warnLimit,
        1.1,
        nowSupplier!!)
}
