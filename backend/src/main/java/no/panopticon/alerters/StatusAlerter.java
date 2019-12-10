package no.panopticon.alerters;

import no.panopticon.integrations.pagerduty.PagerdutyClient;
import no.panopticon.integrations.slack.SlackClient;
import no.panopticon.storage.RunningUnit;
import no.panopticon.storage.StatusSnapshot;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Service
public class StatusAlerter {

    private final SlackClient slackClient;
    private PagerdutyClient pagerdutyClient;
    private ConcurrentMap<Alertable, String> alertedAbout = new ConcurrentHashMap<>();

    public StatusAlerter(SlackClient slackClient, PagerdutyClient pagerdutyClient) {
        this.slackClient = slackClient;
        this.pagerdutyClient = pagerdutyClient;
    }

    public void handle(RunningUnit unit, StatusSnapshot snapshot, ConcurrentMap<RunningUnit, StatusSnapshot> currentStatuses) {
        handleSingleEventAlerting(unit, snapshot);
        handleCombinedAlerting(currentStatuses);
    }

    private void handleSingleEventAlerting(RunningUnit unit, StatusSnapshot snapshot) {
        List<StatusSnapshot.Measurement> wasWarnErrorIsNowInfo = snapshot.getMeasurements().stream()
                .filter(m -> m.getStatus().equals("INFO"))
                .filter(m -> {
                    String alertable = alertedAbout.get(new Alertable(unit, m.getKey()));
                    return alertable != null;
                })
                .collect(toList());

        List<StatusSnapshot.Measurement> hasChangedAlertLevel = snapshot.getMeasurements().stream()
                .filter(m -> !m.getStatus().equals("INFO"))
                .filter(m -> {
                    String alertable = alertedAbout.get(new Alertable(unit, m.getKey()));
                    return alertable == null || !alertable.equals(m.getStatus());
                })
                .collect(toList());

        wasWarnErrorIsNowInfo.forEach((measurement) -> {
            slackClient.alertAboutStatus(unit, measurement);
            pagerdutyClient.alertAboutStatus(unit, measurement);
        });
        hasChangedAlertLevel.forEach((measurement) -> {
            slackClient.alertAboutStatus(unit, measurement);
            pagerdutyClient.alertAboutStatus(unit, measurement);
        });

        wasWarnErrorIsNowInfo.forEach(m -> alertedAbout.remove(new Alertable(unit, m.getKey())));
        hasChangedAlertLevel.forEach(m -> alertedAbout.put(new Alertable(unit, m.getKey()), m.getStatus()));
    }

    public void handleCombinedAlerting(Map<RunningUnit, StatusSnapshot> statuses) {

        Map<String, List<ThingToAlertAbout>> measurements = new HashMap<>();


        statuses.entrySet().stream()
                .filter(e1 -> !e1.getValue().isOlderThan(5, MINUTES))
                .forEach(e -> {
                    e.getValue().getMeasurements().forEach(m -> {
                        measurements.computeIfAbsent(String.format("%s @ %s", m.key, e.getKey().getComponent()), a -> new ArrayList<>()).add(new ThingToAlertAbout(e.getKey(), m));
                    });
                });

        List<SlackClient.Line> toAlert = new ArrayList<>();

        measurements.entrySet().forEach(m -> {
            String highestSeverity = getHighestSeverity(m);
            if (!highestSeverity.equalsIgnoreCase("INFO")) {
                toAlert.add(new SlackClient.Line(
                        highestSeverity,
                        createHeader(m),
                        createMessage(m)
                ));
            }
        });

        slackClient.combinedStatusAlerting(toAlert);
    }

    private String getHighestSeverity(Map.Entry<String, List<ThingToAlertAbout>> m) {
        if (m.getValue().stream().anyMatch(a -> a.measurement.status.equalsIgnoreCase("ERROR"))) {
            return "ERROR";
        } else if (m.getValue().stream().anyMatch(a -> a.measurement.status.equalsIgnoreCase("WARN"))) {
            return "WARN";
        } else {
            return "INFO";
        }
    }

    private String createHeader(Map.Entry<String, List<ThingToAlertAbout>> m) {
        long numberOfErrors = m.getValue().stream().filter(t -> t.measurement.status.equalsIgnoreCase("ERROR")).count();
        long numberOfWarns = m.getValue().stream().filter(t -> t.measurement.status.equalsIgnoreCase("WARN")).count();
        return String.format("%s: %d x error, %d x warn", m.getKey(), numberOfErrors, numberOfWarns);
    }

    private String createMessage(Map.Entry<String, List<ThingToAlertAbout>> m) {
        return m.getValue().stream().sorted(Comparator.comparing(a -> a.measurement.getStatus())).map(t -> {
            String emoji = t.measurement.getStatus().equalsIgnoreCase("ERROR") ? "🟥️" : "🟨";
            return String.format("%s %s: %s", emoji, t.runningUnit.getServer(), t.measurement.getDisplayValue());
        }).collect(joining("\n"));
    }

    private class ThingToAlertAbout {

        public final RunningUnit runningUnit;
        public final StatusSnapshot.Measurement measurement;

        public ThingToAlertAbout(RunningUnit runningUnit, StatusSnapshot.Measurement measurement) {
            this.runningUnit = runningUnit;
            this.measurement = measurement;
        }
    }

    private class Alertable {

        private final RunningUnit unit;
        private final String key;

        public Alertable(RunningUnit unit, String key) {
            this.unit = unit;
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Alertable alertable = (Alertable) o;
            return Objects.equals(unit, alertable.unit) &&
                    Objects.equals(key, alertable.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(unit, key);
        }
    }
}
