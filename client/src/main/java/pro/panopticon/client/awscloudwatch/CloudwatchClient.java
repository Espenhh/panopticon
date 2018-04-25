package pro.panopticon.client.awscloudwatch;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class CloudwatchClient {

    private final AmazonCloudWatch amazonCloudWatch;

    public CloudwatchClient(HasCloudwatchConfig cloudwatchConfig) {
        amazonCloudWatch = AmazonCloudWatchClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(cloudwatchConfig))
                .withRegion(cloudwatchConfig.getRegion())
                .build();
    }

    public void sendStatistics(String namespace, List<CloudwatchStatistic> statistics) {
        if (statistics == null || statistics.isEmpty()) {
            return;
        }
        Lists.partition(statistics, 15).parallelStream().forEach(l -> postToCloudwatch(namespace, l));
    }

    private void postToCloudwatch(String namespace, List<CloudwatchStatistic> statistics) {
        List<MetricDatum> metricDatumList = statistics.stream()
                .map(CloudwatchStatistic::toMetricsDatum)
                .collect(toList());

        PutMetricDataRequest request = new PutMetricDataRequest();
        request.setNamespace(namespace);
        request.setMetricData(metricDatumList);

        amazonCloudWatch.putMetricData(request);
    }

    public static class CloudwatchStatistic {
        private String key;
        private Double value;
        private StandardUnit unit;
        private Date date;

        public CloudwatchStatistic(String key, Double value, StandardUnit unit, java.util.Date date) {
            this.key = key;
            this.value = value;
            this.unit = unit;
            this.date = date;
        }

        public CloudwatchStatistic(String key, Double value, StandardUnit unit) {
            this(key, value, unit, new Date());
        }

        public CloudwatchStatistic(String key, Double value) {
            this(key, value, StandardUnit.Count, new Date());
        }

        public MetricDatum toMetricsDatum() {
            MetricDatum metricDatum = new MetricDatum();
            metricDatum.setMetricName(key);
            metricDatum.setTimestamp(date);
            metricDatum.setUnit(unit);
            metricDatum.setValue(value);
            return metricDatum;
        }
    }
}
