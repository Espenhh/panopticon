package no.panopticon.alerters;

import no.panopticon.integrations.slack.PagerdutyClient;
import no.panopticon.integrations.slack.SlackClient;
import no.panopticon.storage.RunningUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static java.util.stream.Collectors.toList;


@Service
public class MissingRunningUnitsAlerter {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final SlackClient slackClient;
    private PagerdutyClient pagerdutyClient;

    private ConcurrentMap<RunningUnit, LocalDateTime> checkins = new ConcurrentHashMap<>();

    private Set<RunningUnit> alertedAbout = new HashSet<>();

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    @Autowired
    public MissingRunningUnitsAlerter(SlackClient slackClient, PagerdutyClient pagerdutyClient) {
        this.slackClient = slackClient;
        this.pagerdutyClient = pagerdutyClient;
        SCHEDULER.scheduleAtFixedRate(this::checkForMissingRunningUnits, 0, 1, TimeUnit.MINUTES);
    }

    private void checkForMissingRunningUnits() {
        checkins.entrySet().forEach(e -> {
            if (e.getValue().isBefore(LocalDateTime.now().minus(5, ChronoUnit.MINUTES)) && !alertedAbout.contains(e.getKey())) {
                alertedAbout.add(e.getKey());
                slackClient.indicateMissingRunningUnit(e.getKey());
                pagerdutyClient.indicateMissingRunningUnit(e.getKey());
            }
        });
        List<RunningUnit> reappeared = alertedAbout.stream()
                .filter(runningUnit -> {
                    LocalDateTime lastSeen = checkins.get(runningUnit);
                    return lastSeen != null && lastSeen.isAfter(LocalDateTime.now().minus(5, ChronoUnit.MINUTES));
                })
                .peek(runningUnit -> {
                    slackClient.indicateReturnedRunningUnit(runningUnit);
                    pagerdutyClient.indicateReturnedRunningUnit(runningUnit);
                })
                .collect(toList());
        alertedAbout.removeAll(reappeared);
    }

    public void checkin(RunningUnit unit) {
        checkins.put(unit, LocalDateTime.now());
    }
}
