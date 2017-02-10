package no.panopticon.storage;

import no.panopticon.api.external.UpdatedStatus;
import no.panopticon.api.internal.UnitDetails;
import no.panopticon.api.internal.UnitSummary;
import no.panopticon.schedulers.MissingRunningUnitsChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.stream.Collectors.toList;

@Service
public class StatusStorage {

    private static final Logger LOG = LoggerFactory.getLogger(StatusStorage.class);

    private final MissingRunningUnitsChecker missingRunningUnitsChecker;

    private ConcurrentMap<RunningUnit, StatusSnapshot> currentStatuses = new ConcurrentHashMap<>();

    @Autowired
    public StatusStorage(MissingRunningUnitsChecker missingRunningUnitsChecker) {
        this.missingRunningUnitsChecker = missingRunningUnitsChecker;
    }

    public void processUpdatedStatus(UpdatedStatus updatedStatus) {
        RunningUnit unit = updatedStatus.toRunningUnit();
        StatusSnapshot snapshot = updatedStatus.toStatusSnapshot();
        currentStatuses.put(unit, snapshot);
        missingRunningUnitsChecker.checkin(unit);
        LOG.info("Updated " + unit + " with " + snapshot);
    }

    public List<UnitSummary> getAllRunningComponents() {
        return currentStatuses.entrySet().stream()
                .map(entry -> UnitSummary.fromStoredStatus(entry.getKey(), entry.getValue()))
                .collect(toList());
    }


    public Optional<UnitDetails> getSingleComponent(String environment, String system, String component, String server) {
        RunningUnit unit = new RunningUnit(environment, system, component, server);
        return Optional.ofNullable(currentStatuses.get(unit)).map(statusSnapshot -> UnitDetails.fromStoredStatus(unit, statusSnapshot));
    }
}
