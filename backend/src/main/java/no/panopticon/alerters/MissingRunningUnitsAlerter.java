package no.panopticon.alerters;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import no.panopticon.integrations.pagerduty.PagerdutyClient;
import no.panopticon.integrations.slack.SlackClient;
import no.panopticon.storage.RunningUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.joining;


@Service
public class MissingRunningUnitsAlerter {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private static final Duration WARN_WHEN_NO_CHEKIN_FOR_THIS_TIME_PERIOD = Duration.of(5, ChronoUnit.MINUTES);

    private final SlackClient slackClient;
    private PagerdutyClient pagerdutyClient;

    private ExpiringMap<Component, LocalDateTime> lastCheckins = ExpiringMap.builder().expiration(1, TimeUnit.HOURS).expirationPolicy(ExpirationPolicy.ACCESSED).build();

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    @Autowired
    public MissingRunningUnitsAlerter(SlackClient slackClient, PagerdutyClient pagerdutyClient) {
        this.slackClient = slackClient;
        this.pagerdutyClient = pagerdutyClient;
        SCHEDULER.scheduleAtFixedRate(this::checkForMissingRunningUnits, 0, 1, TimeUnit.MINUTES);
    }

    private void checkForMissingRunningUnits() {

        Map<Component, LocalDateTime> expiredComponents = new HashMap<>();
        Map<Component, LocalDateTime> notExpiredComponents = new HashMap<>();

        lastCheckins.forEach((c, e) -> {
            if (e.isBefore(LocalDateTime.now().minus(WARN_WHEN_NO_CHEKIN_FOR_THIS_TIME_PERIOD))) {
                expiredComponents.put(c, e);
            } else {
                notExpiredComponents.put(c, e);
            }
        });

        expiredComponents.forEach((c, e) -> {
            slackClient.indicateNoRunningUnits(c);
            pagerdutyClient.indicateNoRunningUnits(c);
        });

        notExpiredComponents.forEach((c, e) -> {
            pagerdutyClient.indicateRunningUnits(c);
        });

        LOG.info(String.format("Checking expired components. Total: %d, not expired: %d, expired: %d\n\tNOT EXPIRED - last checkins:\n\t%s\n\tEXPIRED - last checkins:\n\t%s",
                expiredComponents.size() + notExpiredComponents.size(),
                notExpiredComponents.size(),
                expiredComponents.size(),
                notExpiredComponents.entrySet().stream().map(e -> e.getKey().describe() + ": " + e.getValue()).collect(joining("\n\t")),
                expiredComponents.entrySet().stream().map(e -> e.getKey().describe() + ": " + e.getValue()).collect(joining("\n\t"))
        ));
    }

    public void checkin(RunningUnit unit) {
        Component component = Component.fromRunningUnit(unit);
        lastCheckins.put(component, LocalDateTime.now());
        lastCheckins.resetExpiration(component);
    }


    public static class Component {

        private final String environment;
        private final String system;
        private final String component;

        public Component(String environment, String system, String component) {
            this.environment = environment;
            this.system = system;
            this.component = component;
        }

        public String getEnvironment() {
            return environment;
        }

        public String getSystem() {
            return system;
        }

        public String getComponent() {
            return component;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Component that = (Component) o;
            return Objects.equals(environment, that.environment) &&
                    Objects.equals(system, that.system) &&
                    Objects.equals(component, that.component);
        }

        @Override
        public int hashCode() {
            return Objects.hash(environment, system, component);
        }

        public static Component fromRunningUnit(RunningUnit unit) {
            return new Component(unit.getEnvironment(), unit.getSystem(), unit.getComponent());
        }

        public String describe() {
            return String.format("%s: %s (%s)", environment, component, system);
        }
    }

}
