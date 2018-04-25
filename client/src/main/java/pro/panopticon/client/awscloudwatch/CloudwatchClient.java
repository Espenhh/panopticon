package pro.panopticon.client.awscloudwatch;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class CloudwatchClient {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

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

        long before = System.currentTimeMillis();

        List<List<CloudwatchStatistic>> partitions = Lists.partition(statistics, 15);

        partitions.parallelStream()
                .forEach(l -> postToCloudwatch(namespace, l));

        long duration = System.currentTimeMillis() - before;

        LOG.info(String.format("Sent %d partitions to CloudWatch for namespace %s in %dms", partitions.size(), namespace, duration));
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
