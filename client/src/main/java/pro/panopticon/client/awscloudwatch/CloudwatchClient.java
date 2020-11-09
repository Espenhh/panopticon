package pro.panopticon.client.awscloudwatch;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.panopticon.client.model.MetricDimension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class CloudwatchClient {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final AmazonCloudWatch amazonCloudWatch;

    public CloudwatchClient(HasCloudwatchConfig cloudwatchConfig) {
        AmazonCloudWatchClientBuilder clientBuilder = AmazonCloudWatchClientBuilder.standard();
        if (!Strings.isNullOrEmpty(cloudwatchConfig.getRegion())) {
            clientBuilder.withRegion(Regions.fromName(cloudwatchConfig.getRegion()));
        }
        if (credentialsProvided(cloudwatchConfig)) {
            clientBuilder.withCredentials(new AWSStaticCredentialsProvider(cloudwatchConfig));
        }

        amazonCloudWatch = clientBuilder.build();
    }

    private boolean credentialsProvided(HasCloudwatchConfig cloudwatchConfig) {
        boolean hasAccessKey = !Strings.isNullOrEmpty(cloudwatchConfig.getAWSAccessKeyId());
        boolean hasSecretKey = !Strings.isNullOrEmpty(cloudwatchConfig.getAWSSecretKey());
        if (hasAccessKey != hasSecretKey) {
            throw new IllegalArgumentException("Either Access Key ID or Secret Key is missing. Please provide both, " +
                    "or neither if you want to defer to DefaultAWSCredentialsProviderChain");
        }
        return hasAccessKey;
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
        private List<MetricDimension> dimensions = new ArrayList<>();

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

        public CloudwatchStatistic withDimensions(List<MetricDimension> dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        public MetricDatum toMetricsDatum() {
            MetricDatum metricDatum = new MetricDatum();
            metricDatum.setMetricName(key);
            metricDatum.setTimestamp(date);
            metricDatum.setUnit(unit);
            metricDatum.setValue(value);
            metricDatum.setDimensions(mapToAwsDimension());
            return metricDatum;
        }

        private List<com.amazonaws.services.cloudwatch.model.Dimension> mapToAwsDimension() {
            return dimensions.stream().map(dimension -> {
                com.amazonaws.services.cloudwatch.model.Dimension awsDimension = new com.amazonaws.services.cloudwatch.model.Dimension();
                awsDimension.setName(dimension.name);
                awsDimension.setValue(dimension.value);
                return awsDimension;
            }).collect(Collectors.toList());
        }
    }
}
