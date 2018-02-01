package no.panopticon.api.external.sns;

import java.util.List;

public class Trigger {

    public String MetricName;
    public String Namespace;
    public String StatisticType;
    public String Statistic;
    public Object Unit;
    public List<Dimension> Dimensions = null;
    public Integer Period;
    public Integer EvaluationPeriods;
    public String ComparisonOperator;
    public Float Threshold;
    public String TreatMissingData;
    public String EvaluateLowSampleCountPercentile;


    @Override
    public String toString() {
        return "Trigger{" +
                "MetricName='" + MetricName + '\'' +
                ", Namespace='" + Namespace + '\'' +
                ", StatisticType='" + StatisticType + '\'' +
                ", Statistic='" + Statistic + '\'' +
                ", Unit=" + Unit +
                ", Dimensions=" + Dimensions +
                ", Period=" + Period +
                ", EvaluationPeriods=" + EvaluationPeriods +
                ", ComparisonOperator='" + ComparisonOperator + '\'' +
                ", Threshold=" + Threshold +
                ", TreatMissingData='" + TreatMissingData + '\'' +
                ", EvaluateLowSampleCountPercentile='" + EvaluateLowSampleCountPercentile + '\'' +
                '}';
    }

}
