package no.panopticon.integrations.pagerduty;

import com.github.dikhan.pagerduty.client.events.PagerDutyEventsClient;
import com.github.dikhan.pagerduty.client.events.domain.Payload;
import com.github.dikhan.pagerduty.client.events.domain.ResolveIncident;
import com.github.dikhan.pagerduty.client.events.domain.Severity;
import com.github.dikhan.pagerduty.client.events.domain.TriggerIncident;
import com.github.dikhan.pagerduty.client.events.exceptions.NotifyEventException;
import no.panopticon.alerters.MissingRunningUnitsAlerter;
import no.panopticon.config.PagerdutyConfiguration;
import no.panopticon.storage.RunningUnit;
import no.panopticon.storage.StatusSnapshot;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.stream.Stream;

@Service
public class PagerdutyClientV2 implements PagerdutyClient {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final PagerDutyEventsClient client;
    private PagerdutyConfiguration configuration;

    public PagerdutyClientV2(PagerdutyConfiguration configuration) {
        this.configuration = configuration;
        client = PagerDutyEventsClient.create();
    }

    @Override
    public void alertAboutStatus(RunningUnit runningUnit, StatusSnapshot.Measurement measurement) {
        if (!shouldAlert(runningUnit.getEnvironment())) {
            return;
        }
        if (measurement.getStatus().equals("ERROR")) {
            alertAboutStatusTrigger(runningUnit, measurement);
        } else {
            alertAboutStatusResolve(runningUnit, measurement);
        }
    }

    @Override
    public void indicateNoRunningUnits(MissingRunningUnitsAlerter.Component c) {
        if (!shouldAlert(c.getEnvironment())) {
            return;
        }

        HashMap<String, String> details = new HashMap<>();
        details.put("environment", c.getEnvironment());

        Payload payload = Payload.Builder.newBuilder()
                .setSummary(String.format("%s - The app has 0 instances running!", createPrettyName(c)))
                .setSeverity(Severity.CRITICAL)
                .setComponent(c.getComponent())
                .setCustomDetails(new JSONObject(details))
                .build();
        TriggerIncident incident = TriggerIncident.TriggerIncidentBuilder
                .newBuilder(configuration.integrationKey, payload)
                .setDedupKey(createDedupKey(c))
                .build();

        try {
            client.trigger(incident);
        } catch (NotifyEventException e) {
            LOG.warn("Error when calling pagerduty to trigger missing running unit", e);
        }
    }


    @Override
    public void indicateRunningUnits(MissingRunningUnitsAlerter.Component c) {
        if (!shouldAlert(c.getEnvironment())) {
            return;
        }

        HashMap<String, String> details = new HashMap<>();
        details.put("environment", c.getEnvironment());

        Payload payload = Payload.Builder.newBuilder()
                .setSummary(String.format("%s - The app has units running again :)", createPrettyName(c)))
                .setSeverity(Severity.CRITICAL)
                .setComponent(c.getComponent())
                .setCustomDetails(new JSONObject(details))
                .build();

        ResolveIncident resolution = ResolveIncident.ResolveIncidentBuilder
                .newBuilder(configuration.integrationKey, createDedupKey(c))
                .setPayload(payload)
                .build();

        try {
            client.resolve(resolution);
        } catch (NotifyEventException e) {
            LOG.warn("Error when calling pagerduty to trigger missing running unit", e);
        }
    }

    private void alertAboutStatusTrigger(RunningUnit runningUnit, StatusSnapshot.Measurement measurement) {
        Payload payload = createPayload(runningUnit, measurement);

        TriggerIncident incident = TriggerIncident.TriggerIncidentBuilder
                .newBuilder(configuration.integrationKey, payload)
                .setDedupKey(createDedupKey(runningUnit, measurement))
                .build();

        try {
            client.trigger(incident);
        } catch (NotifyEventException e) {
            LOG.warn("Error when calling pagerduty to trigger measurement", e);
        }
    }

    private void alertAboutStatusResolve(RunningUnit runningUnit, StatusSnapshot.Measurement measurement) {
        Payload payload = createPayload(runningUnit, measurement);

        ResolveIncident resolution = ResolveIncident.ResolveIncidentBuilder
                .newBuilder(configuration.integrationKey, createDedupKey(runningUnit, measurement))
                .setPayload(payload)
                .build();

        try {
            client.resolve(resolution);
        } catch (NotifyEventException e) {
            LOG.warn("Error when calling pagerduty to resolve measurement", e);
        }
    }

    private Payload createPayload(RunningUnit runningUnit, StatusSnapshot.Measurement measurement) {
        JSONObject details = createCustomDetails(runningUnit, measurement);

        return Payload.Builder.newBuilder()
                .setSummary(String.format("The measurement '%s' is in status %s", measurement.getKey(), measurement.getStatus()))
                .setSource(runningUnit.getServer())
                .setSeverity(Severity.CRITICAL)
                .setComponent(runningUnit.getComponent())
                .setCustomDetails(details)
                .build();
    }

    private JSONObject createCustomDetails(RunningUnit runningUnit, StatusSnapshot.Measurement measurement) {
        HashMap<String, String> details = new HashMap<>();
        details.put("measurment-numeric", String.valueOf(measurement.getNumericValue()));
        details.put("measurement-value", measurement.getDisplayValue());
        details.put("measurement-status", measurement.getStatus());
        details.put("measurement-key", measurement.getKey());
        details.put("system", runningUnit.getSystem());
        details.put("environment", runningUnit.getEnvironment());

        return new JSONObject(details);
    }

    private String createDedupKey(RunningUnit runningUnit, StatusSnapshot.Measurement measurement) {
        return String.format("%s-%s-%s", runningUnit.getEnvironment(), runningUnit.getComponent(), measurement.getKey());
    }

    private String createDedupKey(MissingRunningUnitsAlerter.Component c) {
        return String.format("missingunit-%s-%s", c.getEnvironment(), c.getComponent());
    }

    private String createPrettyName(MissingRunningUnitsAlerter.Component c) {
        return String.format("[%s] %s", c.getEnvironment().toUpperCase(), c.getComponent());
    }

    private boolean shouldAlert(String environment) {
        if (configuration.environmentAlertWhitelist == null || environment == null) {
            return false;
        } else {
            return Stream.of(configuration.environmentAlertWhitelist.split(",")).anyMatch(environment::equalsIgnoreCase);
        }
    }
}
