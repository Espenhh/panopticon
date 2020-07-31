package no.panopticon.integrations.pagerduty;

import no.panopticon.config.PagerdutyConfiguration;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PagerdutyClientTest {

    @Test
    public void whitelist_should_ignore_casing() {
        new PagerdutyClientTestHelper()
                .with_environment_alert_whitelist("PROD")
                .should_alert_for("PROD")
                .should_alert_for("prod")
                .should_not_alert_for("TEST1")
                .should_not_alert_for("test1");
    }

    @Test
    public void whitelist_should_support_multiple_values_separated_by_comma() {
        new PagerdutyClientTestHelper()
                .with_environment_alert_whitelist("PROD,STAGING")
                .should_alert_for("PROD")
                .should_alert_for("STAGING")
                .should_not_alert_for("TEST1");
    }

    @Test
    public void whitelist_should_support_being_empty_string() {
        new PagerdutyClientTestHelper()
                .with_environment_alert_whitelist("")
                .should_not_alert_for("PROD")
                .should_not_alert_for("TEST1");
    }

    @Test
    public void whitelist_should_support_being_null() {
        new PagerdutyClientTestHelper()
                .with_environment_alert_whitelist(null)
                .should_not_alert_for("PROD")
                .should_not_alert_for("TEST1");
    }

    @Test
    public void whitelist_should_support_checking_for_null() {
        new PagerdutyClientTestHelper()
                .with_environment_alert_whitelist("PROD")
                .should_not_alert_for(null);
    }

    private class PagerdutyClientTestHelper {

        private PagerdutyClientV1 client;

        PagerdutyClientTestHelper with_environment_alert_whitelist(String whitelist) {
            PagerdutyConfiguration config = new PagerdutyConfiguration();
            config.environmentAlertWhitelist = whitelist;
            config.apikey = "APIKEY";
            client = new PagerdutyClientV1(config);
            return this;
        }

        PagerdutyClientTestHelper should_alert_for(String env) {
            boolean shouldAlert = client.shouldAlert(env);
            assertThat(shouldAlert, is(true));
            return this;
        }

        PagerdutyClientTestHelper should_not_alert_for(String env) {
            boolean shouldAlert = client.shouldAlert(env);
            assertThat(shouldAlert, is(false));
            return this;
        }
    }

}
