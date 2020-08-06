package no.panopticon.integrations.pagerduty;

import no.panopticon.alerters.MissingRunningUnitsAlerter;
import no.panopticon.storage.RunningUnit;
import no.panopticon.storage.StatusSnapshot;

public interface PagerdutyClient {
    void alertAboutStatus(RunningUnit runningUnit, StatusSnapshot.Measurement measurement);

    void indicateNoRunningUnits(MissingRunningUnitsAlerter.Component c);

    void indicateRunningUnits(MissingRunningUnitsAlerter.Component c);
}
