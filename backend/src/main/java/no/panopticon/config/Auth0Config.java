package no.panopticon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth0")
public class Auth0Config {
    public String secret;
    public String issuer;
    public boolean enabled;

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
