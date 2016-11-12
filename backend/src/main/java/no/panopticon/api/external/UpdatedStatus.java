package no.panopticon.api.external;

import no.panopticon.storage.RunningUnit;
import no.panopticon.storage.StatusSnapshot;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class UpdatedStatus {

    public String environment;
    public String system;
    public String component;
    public String server;
    public List<UpdatedMeasurement> measurements;

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

    public RunningUnit toRunningUnit() {
        return new RunningUnit(environment, system, component, server);
    }

    public StatusSnapshot toStatusSnapshot() {
        return new StatusSnapshot(
                measurements.stream()
                        .map(m -> new StatusSnapshot.Measurement(m.key, m.status, m.displayValue, m.numericValue))
                        .collect(toList())
        );
    }

    public static class UpdatedMeasurement {
        public String key;
        public String status;
        public String displayValue;
        public long numericValue;

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
