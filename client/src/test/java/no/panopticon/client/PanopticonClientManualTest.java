package no.panopticon.client;

import no.panopticon.client.model.ComponentInfo;
import no.panopticon.client.model.Measurement;
import no.panopticon.client.model.Status;
import no.panopticon.client.sensor.Sensor;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Ignore("This is a manual test. Run it from the IDE to test against production :)")
public class PanopticonClientManualTest {

    private static final PanopticonClient CLIENT = new PanopticonClient("http://d3554xabuzco0h.cloudfront.net/api");

    @Test
    public void test_add_component_status() {

        List<Measurement> measurements = Arrays.asList(
                new Measurement("jetty.threads", "INFO", "100 av 768 (14%)", 100),
                new Measurement("memory.usage", "WARN", "200MB av 560MB (40%)", 200)
        );

        Status status = new Status("prod", "NSB mobilapps", "MTL", "linuxserver349", measurements);

        CLIENT.update(status);

    }

    @Test
    public void test_run_scheduled_task() throws InterruptedException {
        ComponentInfo componentInfo = new ComponentInfo("prod", "NSB mobilapps", "MTL", "linuxserver349");
        List<Sensor> sensors = Arrays.asList(
                mockSystemSensor(),
                mockJettySensor()
        );
        CLIENT.startScheduledStatusUpdate(componentInfo, sensors);

        Thread.sleep(1000 * 60 * 10);
    }

    private Sensor mockSystemSensor() {
        return () -> {
			List<Measurement> measurements = new ArrayList<>();
			measurements.add(new Measurement("system.load", "INFO", "1.23", 1));
			measurements.add(new Measurement("memory.usage", "WARN", "700 av 1024 MB (78%)", 700));
			return measurements;
		};
    }

    private Sensor mockJettySensor() {
        return () -> {
            List<Measurement> measurements = new ArrayList<>();
            measurements.add(new Measurement("jetty.threads", "INFO", "100 av 768 (14%)", 100));
            return measurements;
        };
    }

}
