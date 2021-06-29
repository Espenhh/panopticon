package pro.panopticon.client.model

import com.amazonaws.services.cloudwatch.model.StandardUnit

data class Measurement(
    val key: String,
    val status: Status,
    val displayValue: String,
    val cloudwatchValue: CloudwatchValue? = null,
    val description: String,
) {
    constructor(
        key: String,
        status: Status,
        displayValue: String,
        description: String,
    ) : this(key, status, displayValue, null, description)

    @Deprecated("String based status is deprecated. Use Status enum instead.")
    constructor(
        key: String,
        status: String,
        displayValue: String,
        description: String,
    ) : this(key, Status.fromString(status), displayValue, null, description)

    @Deprecated("String based status is deprecated. Use Status enum instead.")
    constructor(
        key: String,
        status: String,
        displayValue: String,
        cloudwatchValue: CloudwatchValue?,
        description: String,
    ) : this(key, Status.fromString(status), displayValue, cloudwatchValue, description)

    data class CloudwatchValue @JvmOverloads constructor(
        val value: Double,
        val unit: StandardUnit,
        val dimensions: List<MetricDimension> = emptyList(),
    )

    enum class Status {
        INFO,
        WARN,
        ERROR;

        companion object {
            fun fromString(str: String): Status {
                return when (str) {
                    "INFO" -> INFO
                    "WARN" -> WARN
                    "ERROR" -> ERROR
                    else -> ERROR
                }
            }
        }
    }
}
