package pro.panopticon.client.awscloudwatch;

import com.amazonaws.auth.AWSCredentials;

public interface HasCloudwatchConfig extends AWSCredentials {

    String getAWSAccessKeyId();

    String getAWSSecretKey();

    String getRegion();

    String getNamespace();
}
