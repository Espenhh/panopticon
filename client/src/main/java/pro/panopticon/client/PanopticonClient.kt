package pro.panopticon.client

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import pro.panopticon.client.awscloudwatch.CloudwatchClient
import pro.panopticon.client.awscloudwatch.HasCloudwatchConfig
import pro.panopticon.client.model.ComponentInfo
import pro.panopticon.client.model.Measurement
import pro.panopticon.client.model.Status
import pro.panopticon.client.sensor.Sensor
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PanopticonClient(
    private val baseUri: String,
    private val hasCloudwatchConfig: HasCloudwatchConfig?,
    private val cloudwatchClient: CloudwatchClient?,
) {
    private val LOG = KotlinLogging.logger { }
    private val client: CloseableHttpClient
    private val namespace: String

    fun startScheduledStatusUpdate(componentInfo: ComponentInfo, sensors: List<Sensor>) {
        SCHEDULER.scheduleWithFixedDelay({ performSensorCollection(componentInfo, sensors) }, 0, 1, TimeUnit.MINUTES)
    }

    fun shutdownScheduledStatusUpdate() {
        SCHEDULER.shutdown()
    }

    private fun performSensorCollection(componentInfo: ComponentInfo, sensors: List<Sensor>) {
        try {
            val before = System.currentTimeMillis()
            val measurements = collectMeasurementsFromSensors(sensors)
            val afterMeasurements = System.currentTimeMillis()
            val success = sendMeasurementsToPanopticon(Status(componentInfo, measurements))
            val afterPanopticonPost = System.currentTimeMillis()
            sendSelectMeasurementsToCloudwatch(measurements)
            val afterCloudwatchPost = System.currentTimeMillis()
            val measurementTime = afterMeasurements - before
            val panopticonPostTime = afterPanopticonPost - afterMeasurements
            val cloudwatchPostTime = afterCloudwatchPost - afterPanopticonPost
            if (success) {
                LOG.info(String.format("Sent status update with %d measurements. Fetch measurements took %dms. Posting status to panopticon took %dms. Posting to cloudwatch took %dms",
                    measurements.size,
                    measurementTime,
                    panopticonPostTime,
                    cloudwatchPostTime))
            } else {
                LOG.warn("Could not update status")
            }
        } catch (e: Exception) {
            LOG.warn("Got error when measuring sensors to send to panopticon", e)
        }
    }

    private fun collectMeasurementsFromSensors(sensors: List<Sensor>): List<Measurement> {
        return sensors.flatMap {
            try {
                it.measure()
            } catch (e: Exception) {
                LOG.warn("Got error running sensor: " + it.javaClass.name, e)
                emptyList()
            }
        }
    }

    fun sendMeasurementsToPanopticon(status: Status): Boolean {
        try {
            val json = OBJECT_MAPPER.writeValueAsString(status)
            val uri = "$baseUri/external/status"
            LOG.debug("Updating status: $uri")
            LOG.debug("...with JSON: $json")
            val entity = BasicHttpEntity()
            entity.content = ByteArrayInputStream(json.toByteArray())
            val httpPost = HttpPost(uri)
            httpPost.entity = entity
            httpPost.setHeader("Content-Type", "application/json")
            client.execute(httpPost).use { response ->
                LOG.debug("Response: " + response.statusLine.statusCode)
                return response.statusLine.statusCode < 300
            }
        } catch (e: IOException) {
            LOG.warn("Error when updating status", e)
            return false
        }
    }

    private fun sendSelectMeasurementsToCloudwatch(measurements: List<Measurement>) {
        val statistics = measurements
            .mapNotNull { measurement ->
                measurement.cloudwatchValue?.let { cloudwatchValue ->
                    CloudwatchClient.CloudwatchStatistic(
                        measurement.key,
                        cloudwatchValue.value,
                        cloudwatchValue.unit
                    )
                        .withDimensions(cloudwatchValue.dimensions)
                }
            }
        if (cloudwatchClient != null && hasCloudwatchConfig != null && hasCloudwatchConfig.sensorStatisticsEnabled()) {
            cloudwatchClient.sendStatistics(namespace, statistics)
        }
    }

    private fun createHttpClient(): CloseableHttpClient {
        val requestConfig = RequestConfig.custom()
            .setSocketTimeout(TIMEOUT)
            .setConnectTimeout(TIMEOUT)
            .setConnectionRequestTimeout(TIMEOUT)
            .build()
        return HttpClientBuilder.create()
            .setDefaultRequestConfig(requestConfig)
            .build()
    }

    companion object {
        private const val TIMEOUT = 10000
        private val OBJECT_MAPPER = ObjectMapper()
        private val SCHEDULER = Executors.newScheduledThreadPool(1)
    }

    init {
        client = createHttpClient()
        namespace = String.format("sensor-${hasCloudwatchConfig?.appName}-${hasCloudwatchConfig?.environment}")
    }
}
