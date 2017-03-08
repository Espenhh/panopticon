package no.panopticon.integrations.slack;

import com.squareup.pagerduty.incidents.PagerDuty;
import com.squareup.pagerduty.incidents.Resolution;
import com.squareup.pagerduty.incidents.Trigger;
import no.panopticon.config.PagerdutyConfiguration;
import no.panopticon.storage.RunningUnit;
import no.panopticon.storage.StatusSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PagerdutyClient {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private PagerdutyConfiguration pagerdutyConfiguration;
    private final PagerDuty pagerduty;

    @Autowired
    public PagerdutyClient(PagerdutyConfiguration pagerdutyConfiguration) {
        this.pagerdutyConfiguration = pagerdutyConfiguration;
        pagerduty = PagerDuty.create(pagerdutyConfiguration.apikey);
    }

    public void indicateMissingRunningUnit(RunningUnit runningUnit) {
        try {
            Trigger trigger = new Trigger
                    .Builder(String.format("%s - the instance has been missing for 5 minutes.", createHumanReadableName(runningUnit)))
                    .withIncidentKey(createIncidentKey(runningUnit))
                    .addDetails("server", runningUnit.getServer())
                    .addDetails("component", runningUnit.getComponent())
                    .addDetails("system", runningUnit.getSystem())
                    .addDetails("environment", runningUnit.getEnvironment())
                    .build();
            pagerduty.notify(trigger);
        } catch (IOException e) {
            LOG.warn("Error when calling pagerduty to trigger missing running unit", e);
        }
    }

    public void indicateReturnedRunningUnit(RunningUnit runningUnit) {
        try {
            Resolution resolution = new Resolution
                    .Builder(createIncidentKey(runningUnit))
                    .withDescription(String.format("%s - the missing instance has become active again.", createHumanReadableName(runningUnit)))
                    .addDetails("server", runningUnit.getServer())
                    .addDetails("component", runningUnit.getComponent())
                    .addDetails("system", runningUnit.getSystem())
                    .addDetails("environment", runningUnit.getEnvironment())
                    .build();
            pagerduty.notify(resolution);
        } catch (IOException e) {
            LOG.warn("Error when calling pagerduty for resolution to missing running unit", e);
        }
    }

    public void alertAboutStatus(RunningUnit runningUnit, StatusSnapshot.Measurement measurement) {
        if (measurement.getStatus().equals("ERROR")) {
            alertAboutStatusTrigger(runningUnit, measurement);
        } else {
            alertAboutStatusResolve(runningUnit, measurement);
        }
    }

    private void alertAboutStatusTrigger(RunningUnit runningUnit, StatusSnapshot.Measurement measurement) {
        try {
            Trigger trigger = new Trigger
                    .Builder(String.format("%s - the measurement '%s' is in status %s.", createHumanReadableName(runningUnit), measurement.getKey(), measurement.getStatus()))
                    .withIncidentKey(createIncidentKey(runningUnit, measurement))
                    .addDetails("measurement-numeric", String.valueOf(measurement.getNumericValue()))
                    .addDetails("measurement-value", measurement.getDisplayValue())
                    .addDetails("measurement-status", measurement.getStatus())
                    .addDetails("measurement-key", measurement.getKey())
                    .addDetails("server", runningUnit.getServer())
                    .addDetails("component", runningUnit.getComponent())
                    .addDetails("system", runningUnit.getSystem())
                    .addDetails("environment", runningUnit.getEnvironment())
                    .build();
            pagerduty.notify(trigger);
        } catch (IOException e) {
            LOG.warn("Error when calling pagerduty to trigger measurement", e);
        }
    }

    private void alertAboutStatusResolve(RunningUnit runningUnit, StatusSnapshot.Measurement measurement) {
        try {
            Resolution resolution = new Resolution
                    .Builder(createIncidentKey(runningUnit, measurement))
                    .withDescription(String.format("%s - the measurement '%s' is in status %s.", createHumanReadableName(runningUnit), measurement.getKey(), measurement.getStatus()))
                    .addDetails("measurement-numeric", String.valueOf(measurement.getNumericValue()))
                    .addDetails("measurement-value", measurement.getDisplayValue())
                    .addDetails("measurement-status", measurement.getStatus())
                    .addDetails("measurement-key", measurement.getKey())
                    .addDetails("server", runningUnit.getServer())
                    .addDetails("component", runningUnit.getComponent())
                    .addDetails("system", runningUnit.getSystem())
                    .addDetails("environment", runningUnit.getEnvironment())
                    .build();
            pagerduty.notify(resolution);
        } catch (IOException e) {
            LOG.warn("Error when calling pagerduty to resolve measurement", e);
        }
    }

    private String createHumanReadableName(RunningUnit runningUnit) {
        return String.format("[%s] %s on %s", runningUnit.getEnvironment().toUpperCase(), runningUnit.getComponent(), runningUnit.getServer());
    }

    private String createIncidentKey(RunningUnit runningUnit) {
        return String.format("missingunit-%s-%s-%s-%s", runningUnit.getEnvironment(), runningUnit.getSystem(), runningUnit.getComponent(), runningUnit.getServer());
    }

    private String createIncidentKey(RunningUnit runningUnit, StatusSnapshot.Measurement measurement) {
        return createIncidentKey(runningUnit) + "-" + measurement.getKey();
    }
}
