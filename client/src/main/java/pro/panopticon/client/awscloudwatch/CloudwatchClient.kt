package pro.panopticon.client.awscloudwatch

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder
import com.amazonaws.services.cloudwatch.model.Dimension
import com.amazonaws.services.cloudwatch.model.MetricDatum
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest
import com.amazonaws.services.cloudwatch.model.StandardUnit
import mu.KotlinLogging
import pro.panopticon.client.model.MetricDimension
import java.util.Date

class CloudwatchClient(cloudwatchConfig: HasCloudwatchConfig) {

    private val LOG = KotlinLogging.logger { }
    private val amazonCloudWatch: AmazonCloudWatch

    private fun credentialsProvided(cloudwatchConfig: HasCloudwatchConfig): Boolean {
        val hasAccessKey = cloudwatchConfig.awsAccessKeyId.isNotEmpty()
        val hasSecretKey = cloudwatchConfig.awsSecretKey.isNotEmpty()
        require(hasAccessKey == hasSecretKey) {
            "Either Access Key ID or Secret Key is missing. Please provide both, " +
            "or neither if you want to defer to DefaultAWSCredentialsProviderChain"
        }
        return hasAccessKey
    }

    fun sendStatistics(namespace: String, statistics: List<CloudwatchStatistic>) {
        if (statistics.isNullOrEmpty()) {
            return
        }
        val before = System.currentTimeMillis()
        val partitions = statistics.chunked(15)

        partitions.parallelStream()
            .forEach { postToCloudwatch(namespace, it) }
        val duration = System.currentTimeMillis() - before
        LOG.info("Sent ${partitions.size} partitions to CloudWatch for namespace $namespace in ${duration}ms")
    }

    private fun postToCloudwatch(namespace: String, statistics: List<CloudwatchStatistic>) {
        val metricDatumList = statistics
            .map { it.toMetricsDatum() }

        val request = PutMetricDataRequest()
            .withNamespace(namespace)
            .withMetricData(metricDatumList)

        amazonCloudWatch.putMetricData(request)
    }

    class CloudwatchStatistic @JvmOverloads constructor(
        private val key: String,
        private val value: Double,
        private val unit: StandardUnit = StandardUnit.Count,
        private val date: Date = Date(),
    ) {
        private var dimensions: List<MetricDimension> = emptyList()

        fun withDimensions(dimensions: List<MetricDimension>): CloudwatchStatistic {
            this.dimensions = dimensions
            return this
        }

        fun toMetricsDatum(): MetricDatum {
            return MetricDatum()
                .withMetricName(key)
                .withTimestamp(date)
                .withUnit(unit)
                .withValue(value)
                .withDimensions(mapToAwsDimension())
        }

        private fun mapToAwsDimension(): List<Dimension> {
            return dimensions.map {
                Dimension()
                    .withName(it.name)
                    .withValue(it.value)
            }
        }
    }

    init {
        val clientBuilder = AmazonCloudWatchClientBuilder.standard()
        if (!cloudwatchConfig.region.isNullOrEmpty()) {
            clientBuilder.withRegion(Regions.fromName(cloudwatchConfig.region))
        }
        if (credentialsProvided(cloudwatchConfig)) {
            clientBuilder.withCredentials(AWSStaticCredentialsProvider(cloudwatchConfig))
        }
        amazonCloudWatch = clientBuilder.build()
    }
}
