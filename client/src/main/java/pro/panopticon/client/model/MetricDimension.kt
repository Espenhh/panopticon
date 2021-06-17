package pro.panopticon.client.model

data class MetricDimension private constructor(
    val name: String,
    val value: String,
) {
    companion object {
        private const val PLATFORM_DIMENSION_NAME = "Platform"
        private const val INSTANCE_DIMENSION_NAME = "Instance"

        fun platformDimension(value: String): MetricDimension {
            return MetricDimension(PLATFORM_DIMENSION_NAME, value)
        }

        @JvmStatic
        fun instanceDimension(value: String): MetricDimension {
            return MetricDimension(INSTANCE_DIMENSION_NAME, value)
        }

        fun customDimension(key: String, value: String): MetricDimension {
            return MetricDimension(key, value)
        }
    }
}
