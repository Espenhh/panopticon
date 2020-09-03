package pro.panopticon.client.sensor.impl;

import org.junit.Test;
import pro.panopticon.client.model.Measurement;
import pro.panopticon.client.util.NowSupplier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static pro.panopticon.client.sensor.impl.SuccessrateSensor.HOURS_FOR_ERROR_TICK_TO_BE_CONSIDERED_OUTDATED;

public class SuccessrateSensorTest {

    @Test
    public void should_not_warn_before_reaching_enough_data() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        sensor.tickSuccess(new SuccessrateSensor.AlertInfo("key1", "description"));
        sensor.tickFailure(new SuccessrateSensor.AlertInfo("key1", "description"));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(1));
        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("INFO"));
        assertThat(key1.get().displayValue, containsString("Last 2 calls: 1 success, 1 failure"));
        assertThat(key1.get().displayValue, containsString("not enough calls to report status yet"));
    }

    @Test
    public void should_get_status_info() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        IntStream.range(0, 100).forEach(i -> sensor.tickSuccess(new SuccessrateSensor.AlertInfo("key1", "description")));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(1));
        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("INFO"));
        assertThat(key1.get().displayValue, containsString("Last 100 calls: 100 success, 0 failure"));
    }

    @Test
    public void should_get_status_warn() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        IntStream.range(0, 90).forEach(i -> sensor.tickSuccess(new SuccessrateSensor.AlertInfo("key1", "description")));
        IntStream.range(0, 10).forEach(i -> sensor.tickFailure(new SuccessrateSensor.AlertInfo("key1", "description")));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(1));
        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("WARN"));
        assertThat(key1.get().displayValue, containsString("Last 100 calls: 90 success, 10 failure"));
    }

    @Test
    public void should_get_status_error() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        IntStream.range(0, 80).forEach(i -> sensor.tickSuccess(new SuccessrateSensor.AlertInfo("key1", "description")));
        IntStream.range(0, 20).forEach(i -> sensor.tickFailure(new SuccessrateSensor.AlertInfo("key1", "description")));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(1));
        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("ERROR"));
        assertThat(key1.get().displayValue, containsString("Last 100 calls: 80 success, 20 failure"));
    }

    @Test
    public void should_get_status_info_when_warn_levels_is_null() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, null, null);
        IntStream.range(0, 100).forEach(i -> sensor.tickFailure(new SuccessrateSensor.AlertInfo("key1", "description")));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(1));
        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("INFO"));
        assertThat(key1.get().displayValue, containsString("Last 100 calls: 0 success, 100 failure"));
    }

    @Test
    public void should_only_keep_last_XXX_measurements() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        IntStream.range(0, 100).forEach(i -> sensor.tickFailure(new SuccessrateSensor.AlertInfo("key1", "description")));
        IntStream.range(0, 100).forEach(i -> sensor.tickSuccess(new SuccessrateSensor.AlertInfo("key1", "description")));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(1));
        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("INFO"));
        assertThat(key1.get().displayValue, containsString("Last 100 calls: 100 success, 0 failure"));
    }

    @Test
    public void should_keep_data_for_multiple_keys() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        IntStream.range(0, 50).forEach(i -> sensor.tickSuccess(new SuccessrateSensor.AlertInfo("key1", "description")));
        IntStream.range(0, 50).forEach(i -> sensor.tickFailure(new SuccessrateSensor.AlertInfo("key1", "description")));
        IntStream.range(0, 98).forEach(i -> sensor.tickSuccess(new SuccessrateSensor.AlertInfo("key2", "description")));
        IntStream.range(0, 2).forEach(i -> sensor.tickFailure(new SuccessrateSensor.AlertInfo("key2", "description")));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(2));

        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("ERROR"));
        assertThat(key1.get().displayValue, containsString("Last 100 calls: 50 success, 50 failure"));

        Optional<Measurement> key2 = measurements.stream().filter(m -> m.key.equals("key2")).findAny();
        assertThat(key2.isPresent(), is(true));
        assertThat(key2.get().status, is("INFO"));
        assertThat(key2.get().displayValue, containsString("Last 100 calls: 98 success, 2 failure"));
    }

    @Test
    public void should_display_description_when_present() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        IntStream.range(0, 50).forEach(i -> sensor.tickSuccess(new SuccessrateSensor.AlertInfo("key1", "description")));
        IntStream.range(0, 50).forEach(i -> sensor.tickFailure(new SuccessrateSensor.AlertInfo("key1", "description")));
        List<Measurement> measurements = sensor.measure();

        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.get().description, is("description"));
    }

    @Test
    public void should_ignore_errors_if_all_ticks_are_outdated() {
        NowSupplierMock nowSupplier = new NowSupplierMock();
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2, nowSupplier);

        nowSupplier.mockThePast = true;
        IntStream.range(0, 50).forEach(i -> sensor.tickFailure(new SuccessrateSensor.AlertInfo("key1", "description")));

        nowSupplier.mockThePast = false;
        IntStream.range(0, 50).forEach(i -> sensor.tickSuccess(new SuccessrateSensor.AlertInfo("key1", "description")));
        
        List<Measurement> measurements = sensor.measure();

        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.get().status, is("INFO"));
    }


    class NowSupplierMock implements NowSupplier {
        boolean mockThePast = false;

        @Override
        public LocalDateTime now() {
            if (mockThePast) {
                return LocalDateTime.now().minusHours(HOURS_FOR_ERROR_TICK_TO_BE_CONSIDERED_OUTDATED + 4);
            } else {
                return LocalDateTime.now();
            }
        }
    }
}
