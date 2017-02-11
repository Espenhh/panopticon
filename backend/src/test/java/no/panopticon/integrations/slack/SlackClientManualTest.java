package no.panopticon.integrations.slack;

import no.panopticon.config.SlackConfiguration;
import no.panopticon.storage.RunningUnit;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SlackClientManualTest {

    private SlackClient slack;

    @Before
    public void before() {
        SlackConfiguration slackConfiguration = new SlackConfiguration();
        slackConfiguration.setToken("SECRET");
        slack = new SlackClient(slackConfiguration);
    }

    @Test
    public void skal_() {
        slack.indicateMissingRunningUnit(new RunningUnit("prod", "NSB Salgsapper", "MTL", "linuxserver123"));
        slack.indicateReturnedRunningUnit(new RunningUnit("prod", "NSB Salgsapper", "MTL", "linuxserver123"));
    }

}
