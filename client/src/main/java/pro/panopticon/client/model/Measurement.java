package pro.panopticon.client.model;

import com.amazonaws.services.cloudwatch.model.StandardUnit;

public class Measurement {
    public String key;
    public String status;
    public String displayValue;
    public CloudwatchValue cloudwatchValue;
    public String description;

    public Measurement(String key, String status, String displayValue, CloudwatchValue cloudwatchValue, String description) {
        this.key = key;
        this.status = status;
        this.displayValue = displayValue;
        this.cloudwatchValue = cloudwatchValue;
        this.description = description;
    }

    public Measurement(String key, String status, String displayValue, CloudwatchValue cloudwatchValue) {
        this(key, status, displayValue, cloudwatchValue, null);
    }

    public Measurement(String key, String status, String displayValue) {
        this(key, status, displayValue, null, null);
    }

    @Override
    public String toString() {
        return "Measurement{" +
                "key='" + key + '\'' +
                ", status='" + status + '\'' +
                ", displayValue='" + displayValue + '\'' +
                ", cloudwatchValue=" + cloudwatchValue +
                ", description=" + description +
                '}';
    }

    public static class CloudwatchValue {
        public final double value;
        public final StandardUnit unit;

        public CloudwatchValue(double value, StandardUnit unit) {
            this.value = value;
            this.unit = unit;
        }

    }
}

