package no.panopticon.integrations.slack;

import no.panopticon.config.PagerdutyConfiguration;
import no.panopticon.integrations.pagerduty.PagerdutyClient;
import no.panopticon.storage.RunningUnit;
import no.panopticon.storage.StatusSnapshot;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class PagerdutyClientManualTest {

    private static final RunningUnit RUNNING_UNIT = new RunningUnit("prod", "mobile apps", "MTL", "linuxserver107");

    private PagerdutyClient client;

    @Before
    public void setup() {
        PagerdutyConfiguration config = new PagerdutyConfiguration();
        config.setApikey("SECRET");
        client = new PagerdutyClient(config);
    }

    @Test
    public void indicate_error() {
        client.alertAboutStatus(RUNNING_UNIT, new StatusSnapshot.Measurement("rest.calls", "ERROR", "Last 1000 calls: 500 success, 500 failure (50.00% failure)", 0));
    }

    @Test
    public void indicate_warn() {
        client.alertAboutStatus(RUNNING_UNIT, new StatusSnapshot.Measurement("rest.calls", "WARN", "Last 1000 calls: 800 success, 200 failure (20.00% failure)", 0));
    }

    @Test
    public void indicate_info() {
        client.alertAboutStatus(RUNNING_UNIT, new StatusSnapshot.Measurement("rest.calls", "INFO", "Last 1000 calls: 950 success, 50 failure (5.00% failure)", 0));
    }

}
