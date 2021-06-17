package pro.panopticon.client.model

import com.amazonaws.services.cloudwatch.model.StandardUnit

data class Measurement(
    val key: String,
    val status: String,
    val displayValue: String,
    val cloudwatchValue: CloudwatchValue? = null,
    val description: String,
) {
    constructor(
        key: String,
        status: String,
        displayValue: String,
        description: String,
    ) : this(key, status, displayValue, null, description)

    data class CloudwatchValue @JvmOverloads constructor(
        val value: Double,
        val unit: StandardUnit,
        val dimensions: List<MetricDimension> = emptyList(),
    )
}
