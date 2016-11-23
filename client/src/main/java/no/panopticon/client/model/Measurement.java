package no.panopticon.client.model;

public class Measurement {
    public String key;
    public String status;
    public String displayValue;
    public long numericValue;

    public Measurement(String key, String status, String displayValue, long numericValue) {
        this.key = key;
        this.status = status;
        this.displayValue = displayValue;
        this.numericValue = numericValue;
    }

    @Override
    public String toString() {
        return "UpdatedMeasurement{" +
                "key='" + key + '\'' +
                ", status='" + status + '\'' +
                ", displayValue='" + displayValue + '\'' +
                ", numericValue=" + numericValue +
                '}';
    }
}

