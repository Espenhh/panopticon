package no.panopticon.api.internal;

import no.panopticon.api.storage.RunningUnit;
import no.panopticon.api.storage.StatusSnapshot;

import static no.panopticon.api.internal.InternalStatusResource.INTERNAL_STATUS_BASE_PATH;

public class UnitSummary {
    public final String environment;
    public final String system;
    public final String component;
    public final String server;
    public final String overallStatus;
    public final Links links;

    public UnitSummary(String environment, String system, String component, String server, String overallStatus, Links links) {
        this.environment = environment;
        this.system = system;
        this.component = component;
        this.server = server;
        this.overallStatus = overallStatus;
        this.links = links;
    }

    public static UnitSummary fromStoredStatus(RunningUnit unit, StatusSnapshot snapshot) {
        return new UnitSummary(
                unit.getEnvironment(),
                unit.getSystem(),
                unit.getComponent(),
                unit.getServer(),
                snapshot.mostSevereStatus(),
                new Links(
                        INTERNAL_STATUS_BASE_PATH + "/" + unit.getEnvironment() + "/" + unit.getSystem() + "/" + unit.getComponent() + "/" + unit.getServer()
                )
        );
    }

    public static class Links {
        public final String details;

        public Links(String details) {
            this.details = details;
        }
    }
}
