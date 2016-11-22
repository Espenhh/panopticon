package no.panopticon.client;

import no.panopticon.client.model.Status;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class PanopticonClientManualTest {

    @Test
    @Ignore("This is a manual test. Run it from the IDE to test against production :)")
    public void test_add_component_status() {
        PanopticonClient client = new PanopticonClient("http://d3554xabuzco0h.cloudfront.net/api");

        List<Status.Measurement> measurements = Arrays.asList(
                new Status.Measurement("jetty.threads", "INFO", "100 av 768 (14%)", 100),
                new Status.Measurement("memory.usage", "WARN", "200MB av 560MB (40%)", 200)
        );

        Status status = new Status("prod", "NSB mobilapps", "MTL", "linuxserver349", measurements);

        client.update(status);
    }

}
