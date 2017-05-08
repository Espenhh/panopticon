package no.panopticon.filter;

import no.panopticon.config.Auth0Config;
import no.panopticon.integrations.Auth0Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;
import java.util.Optional;

@AuthenticatedWithAuth0
@Component
public class AuthenticatedWithAuth0Filter implements ContainerRequestFilter {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final Auth0Service service;
    private final boolean enabled;

    @Autowired
    public AuthenticatedWithAuth0Filter(Auth0Config config, Auth0Service service) {
        this.enabled = config.enabled;
        this.service = service;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        if (enabled) {
            getToken(containerRequestContext.getHeaderString("authorization"))
                    .flatMap(service::verify)
                    .orElseThrow(ForbiddenException::new);
        }
    }

    private Optional<String> getToken(String authHeader) {
        if (authHeader == null) {
            LOG.info("No authorization header found");
            return Optional.empty();
        }

        String token = authHeader.replace("Bearer ", "");
        return Optional.of(token);
    }
}
