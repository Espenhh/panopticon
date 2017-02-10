package no.panopticon.storage;

import java.util.List;

public class StatusSnapshot {

    private final List<Measurement> measurements;

    public StatusSnapshot(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public String mostSevereStatus() {
        if (numberOfErrors() > 0) {
            return "ERROR";
        } else if (numberOfWarns() > 0) {
            return "WARN";
        } else {
            return "INFO";
        }
    }

    private long numberOfErrors() {
        return measurements.stream().filter(s -> s.status.equals("ERROR")).count();
    }

    private long numberOfWarns() {
        return measurements.stream().filter(s -> s.status.equals("WARN")).count();
    }

    public static class Measurement {
        private final String key;
        private final String status;
        private final String displayValue;
        private final long numericValue;

        public Measurement(String key, String status, String displayValue, long numericValue) {
            this.key = key;
            this.status = status;
            this.displayValue = displayValue;
            this.numericValue = numericValue;
        }

        public String getKey() {
            return key;
        }

        public String getStatus() {
            return status;
        }

        public String getDisplayValue() {
            return displayValue;
        }

        public long getNumericValue() {
            return numericValue;
        }
    }
}
