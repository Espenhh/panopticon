package pro.panopticon.client.awscloudwatch

import com.amazonaws.auth.AWSCredentials

interface HasCloudwatchConfig : AWSCredentials {
    override fun getAWSAccessKeyId(): String
    override fun getAWSSecretKey(): String
    val region: String?
    fun auditeventStatisticsEnabled(): Boolean
    fun sensorStatisticsEnabled(): Boolean
    val appName: String?
    val environment: String?
}
