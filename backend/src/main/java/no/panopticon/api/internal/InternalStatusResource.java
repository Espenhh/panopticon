package no.panopticon.api.internal;

import no.panopticon.storage.StatusStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Component
@Path(InternalStatusResource.INTERNAL_STATUS_BASE_PATH)
public class InternalStatusResource {

    public static final String INTERNAL_STATUS_BASE_PATH = "/internal/status";

    private final StatusStorage statusStorage;

    @Autowired
    public InternalStatusResource(StatusStorage statusStorage) {
        this.statusStorage = statusStorage;
    }

    @GET
    public Response getAllComponents() {
        return Response.ok(statusStorage.getAllRunningComponents()).build();
    }

    @GET
    @Path("/{environment}/{system}/{component}/{server}")
    public Response getSingleComponent(
            @PathParam("environment") String environment,
            @PathParam("system") String system,
            @PathParam("component") String component,
            @PathParam("server") String server
    ) {
        return statusStorage.getSingleComponent(environment, system, component, server)
                .map(singleComponent -> Response.ok(singleComponent).build())
                .orElse(Response.status(NOT_FOUND).build());
    }

}
