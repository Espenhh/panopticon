package no.panopticon.api.internal;

import no.panopticon.storage.RunningUnit;
import no.panopticon.storage.StatusSnapshot;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class UnitDetails {
    public final String environment;
    public final String system;
    public final String component;
    public final String server;
    public final List<Measurement> measurements;

    public UnitDetails(String environment, String system, String component, String server, List<Measurement> measurements) {
        this.environment = environment;
        this.system = system;
        this.component = component;
        this.server = server;
        this.measurements = measurements;
    }

    public static UnitDetails fromStoredStatus(RunningUnit unit, StatusSnapshot statusSnapshot) {
        return new UnitDetails(
                unit.getEnvironment(),
                unit.getSystem(),
                unit.getComponent(),
                unit.getServer(),
                statusSnapshot.getMeasurements().stream()
                        .map(s -> new Measurement(s.getKey(), s.getStatus(), s.getDisplayValue(), s.getNumericValue()))
                        .sorted(comparing(m -> m.key))
                        .collect(Collectors.toList())
        );
    }

    public static class Measurement {
        public final String key;
        public final String status;
        public final String displayValue;
        public final long numericValue;

        public Measurement(String key, String status, String displayValue, long numericValue) {
            this.key = key;
            this.status = status;
            this.displayValue = displayValue;
            this.numericValue = numericValue;
        }
    }
}
