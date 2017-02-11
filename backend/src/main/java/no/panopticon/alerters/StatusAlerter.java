package no.panopticon.alerters;

import no.panopticon.integrations.slack.SlackClient;
import no.panopticon.storage.RunningUnit;
import no.panopticon.storage.StatusSnapshot;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.stream.Collectors.toList;

@Service
public class StatusAlerter {

    private final SlackClient slackClient;
    private ConcurrentMap<Alertable, String> alertedAbout = new ConcurrentHashMap<>();

    public StatusAlerter(SlackClient slackClient) {
        this.slackClient = slackClient;
    }

    public void handle(RunningUnit unit, StatusSnapshot snapshot) {
        List<StatusSnapshot.Measurement> wasWarnErrorIsNotInfo = snapshot.getMeasurements().stream()
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

        wasWarnErrorIsNotInfo.forEach((measurement) -> slackClient.alertAboutStatus(unit, measurement));
        hasChangedAlertLevel.forEach((measurement) -> slackClient.alertAboutStatus(unit, measurement));

        wasWarnErrorIsNotInfo.forEach(m -> alertedAbout.remove(new Alertable(unit, m.getKey())));
        hasChangedAlertLevel.forEach(m -> alertedAbout.put(new Alertable(unit, m.getKey()), m.getStatus()));
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
