package no.panopticon.client.model;

import java.util.List;

public class Status {

    public String environment;
    public String system;
    public String component;
    public String server;
    public List<Measurement> measurements;

    public Status(String environment, String system, String component, String server, List<Measurement> measurements) {
        this.environment = environment;
        this.system = system;
        this.component = component;
        this.server = server;
        this.measurements = measurements;
    }

    @Override
    public String toString() {
        return "UpdatedStatus{" +
                "environment='" + environment + '\'' +
                ", system='" + system + '\'' +
                ", component='" + component + '\'' +
                ", server='" + server + '\'' +
                ", measurements=" + measurements +
                '}';
    }

    public static class Measurement {
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
}

