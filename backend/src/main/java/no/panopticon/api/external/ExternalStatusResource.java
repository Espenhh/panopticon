package no.panopticon.api.external;

import no.panopticon.api.storage.StatusStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.CREATED;

@Component
@Path("/external/status")
public class ExternalStatusResource {

    private final StatusStorage statusStorage;

    @Autowired
    public ExternalStatusResource(StatusStorage statusStorage) {
        this.statusStorage = statusStorage;
    }

    private static final Logger LOG = LoggerFactory.getLogger(ExternalStatusResource.class);

    @POST
    public Response updateServerStatus(UpdatedStatus updatedStatus) {
        LOG.info("Received updated status: " + updatedStatus);
        statusStorage.processUpdatedStatus(updatedStatus);
        return Response.status(CREATED).build();
    }

}
