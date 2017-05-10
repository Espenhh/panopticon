package no.panopticon.integrations;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import no.panopticon.config.Auth0Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

@Service
public class Auth0Service {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final JWTVerifier verifier;

    @Autowired
    public Auth0Service(Auth0Config auth0Config) throws UnsupportedEncodingException {
        verifier = JWT.require(Algorithm.HMAC256(auth0Config.secret))
                .withIssuer(auth0Config.issuer)
                .build();
    }

    public Optional<DecodedJWT> verify(String token) {
        try {
            return Optional.of(verifier.verify(token));
        } catch (JWTVerificationException e) {
            LOG.info("Could not verify token: " + token, e);
            return Optional.empty();
        }
    }
}
