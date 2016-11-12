package no.panopticon.api.storage;

import no.panopticon.api.external.UpdatedStatus;
import no.panopticon.api.internal.UnitDetails;
import no.panopticon.api.internal.UnitSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class StatusStorage {

    private static final Logger LOG = LoggerFactory.getLogger(StatusStorage.class);

    private Map<RunningUnit, StatusSnapshot> currentStatuses = new HashMap<>();

    public void processUpdatedStatus(UpdatedStatus updatedStatus) {
        RunningUnit unit = updatedStatus.toRunningUnit();
        StatusSnapshot snapshot = updatedStatus.toStatusSnapshot();
        currentStatuses.put(unit, snapshot);
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
