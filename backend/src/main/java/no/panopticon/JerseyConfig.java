package no.panopticon;

import no.panopticon.api.external.ExternalStatusResource;
import no.panopticon.api.internal.InternalStatusResource;
import no.panopticon.filter.AuthenticatedWithAuth0Filter;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(ExternalStatusResource.class);
        register(InternalStatusResource.class);
        register(AuthenticatedWithAuth0Filter.class);
    }

}
