package pro.panopticon.client.awscloudwatch;

import com.amazonaws.auth.AWSCredentials;

public interface HasCloudwatchConfig extends AWSCredentials {

    String getAWSAccessKeyId();

    String getAWSSecretKey();

    String getRegion();

    boolean auditeventStatisticsEnabled();

    boolean sensorStatisticsEnabled();

    String getAppName();

    String getEnvironment();

}
