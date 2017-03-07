package no.panopticon.storage;

import no.panopticon.alerters.StatusAlerter;
import no.panopticon.api.external.UpdatedStatus;
import no.panopticon.api.internal.UnitDetails;
import no.panopticon.api.internal.UnitSummary;
import no.panopticon.alerters.MissingRunningUnitsAlerter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

import static java.util.stream.Collectors.toList;

@Service
public class StatusStorage {

    private static final Logger LOG = LoggerFactory.getLogger(StatusStorage.class);

    private final MissingRunningUnitsAlerter missingRunningUnitsAlerter;
    private final StatusAlerter statusAlerter;

    private ConcurrentMap<RunningUnit, StatusSnapshot> currentStatuses = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    @Autowired
    public StatusStorage(MissingRunningUnitsAlerter missingRunningUnitsAlerter, StatusAlerter statusAlerter) {
        this.missingRunningUnitsAlerter = missingRunningUnitsAlerter;
        this.statusAlerter = statusAlerter;

        SCHEDULER.scheduleAtFixedRate(this::removeOldRunningUnits, 0, 1, TimeUnit.MINUTES);
    }

    private void removeOldRunningUnits() {
        List<RunningUnit> toRemove = currentStatuses.entrySet().stream()
                .filter(e -> e.getValue().isOlderThan(24, ChronoUnit.HOURS))
                .map(Map.Entry::getKey)
                .collect(toList());

        toRemove.forEach(r -> {
            LOG.info("Removing old missing instance: " + r);
            currentStatuses.remove(r);
        });
    }

    public void processUpdatedStatus(UpdatedStatus updatedStatus) {
        RunningUnit unit = updatedStatus.toRunningUnit();
        StatusSnapshot snapshot = updatedStatus.toStatusSnapshot();
        currentStatuses.put(unit, snapshot);
        missingRunningUnitsAlerter.checkin(unit);
        statusAlerter.handle(unit, snapshot);
        LOG.info("Updated " + unit + " with " + snapshot);
    }

    public List<UnitSummary> getAllRunningComponents() {
        return currentStatuses.entrySet().stream()
                .map(entry -> UnitSummary.fromStoredStatus(entry.getKey(), entry.getValue()))
                .collect(toList());
    }


    public Optional<UnitDetails> getSingleComponent(String environment, String system, String component, String server) {
        RunningUnit unit = new RunningUnit(environment, system, component, server);
        return Optional.ofNullable(currentStatuses.get(unit))
                .map(statusSnapshot -> UnitDetails.fromStoredStatus(unit, statusSnapshot));
    }
}
