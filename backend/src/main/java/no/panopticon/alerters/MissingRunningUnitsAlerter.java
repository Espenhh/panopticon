package no.panopticon.alerters;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import no.panopticon.integrations.slack.PagerdutyClient;
import no.panopticon.integrations.slack.SlackClient;
import no.panopticon.storage.RunningUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.*;

import static java.time.temporal.ChronoUnit.MINUTES;


@Service
public class MissingRunningUnitsAlerter {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final SlackClient slackClient;
    private PagerdutyClient pagerdutyClient;

    private ConcurrentMap<Component, ExpiringMap<String, LocalDateTime>> checkins = new ConcurrentHashMap<>();
    private ConcurrentMap<Component, Integer> numberOfServers = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    @Autowired
    public MissingRunningUnitsAlerter(SlackClient slackClient, PagerdutyClient pagerdutyClient) {
        this.slackClient = slackClient;
        this.pagerdutyClient = pagerdutyClient;
        SCHEDULER.scheduleAtFixedRate(this::checkForMissingRunningUnits, 0, 1, TimeUnit.MINUTES);
    }

    private void checkForMissingRunningUnits() {
        checkins.forEach((c, m) -> {
            int numberOfServersLastTime = numberOfServers.computeIfAbsent(c, comp -> 0);
            int numberOfServersNow = m.size();
            int numberOfServersNowActiveForMoreThan10Minutes = (int) m.entrySet().stream().filter(e -> e.getValue().isBefore(LocalDateTime.now().minus(10, MINUTES))).count();
            if (numberOfServersNow < numberOfServersLastTime) {
                slackClient.indicateFewerRunningUnits(c, numberOfServersLastTime, numberOfServersNow);
                pagerdutyClient.indicateFewerRunningUnits(c, numberOfServersLastTime, numberOfServersNow);
            } else if (numberOfServersNow > numberOfServersLastTime) {
                pagerdutyClient.indicateMoreRunningUnits(c, numberOfServersLastTime, numberOfServersNow);
            }
            numberOfServers.put(c, numberOfServersNowActiveForMoreThan10Minutes);
        });
    }

    public void checkin(RunningUnit unit) {
        ExpiringMap<String, LocalDateTime> servers = checkins.computeIfAbsent(Component.fromRunningUnit(unit), component -> ExpiringMap.builder().expiration(5, TimeUnit.MINUTES).expirationPolicy(ExpirationPolicy.CREATED).build());
        servers.putIfAbsent(unit.getServer(), LocalDateTime.now());
        servers.resetExpiration(unit.getServer());
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
    }

}
