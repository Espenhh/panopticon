package pro.panopticon.client.awscloudwatch;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;

import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class CloudwatchClient {

    private final AmazonCloudWatch cloudFront;
    private HasCloudwatchConfig cloudwatchConfig;

    public CloudwatchClient(HasCloudwatchConfig cloudwatchConfig) {
        cloudFront = AmazonCloudWatchClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(cloudwatchConfig))
                .withRegion(cloudwatchConfig.getRegion())
                .build();
        this.cloudwatchConfig = cloudwatchConfig;
    }

    public void sendStatistics(List<CloudwatchStatistic> statistics) {
        if (statistics == null || statistics.isEmpty()) {
            return;
        }
        List<MetricDatum> metricDatumList = statistics.stream()
                .map(CloudwatchStatistic::toMetricsDatum)
                .collect(toList());

        PutMetricDataRequest request = new PutMetricDataRequest();
        request.setNamespace(cloudwatchConfig.getNamespace());
        request.setMetricData(metricDatumList);

        cloudFront.putMetricData(request);
    }

    public static class CloudwatchStatistic {
        private String key;
        private Double value;

        public CloudwatchStatistic(String key, Double value) {
            this.key = key;
            this.value = value;
        }

        public MetricDatum toMetricsDatum() {
            MetricDatum metricDatum = new MetricDatum();
            metricDatum.setMetricName(key);
            metricDatum.setTimestamp(new Date());
            metricDatum.setUnit(StandardUnit.Count);
            metricDatum.setValue(value);
            return metricDatum;
        }
    }
}
