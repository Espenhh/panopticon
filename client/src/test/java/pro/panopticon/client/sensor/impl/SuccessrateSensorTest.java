package pro.panopticon.client.sensor.impl;

import org.junit.Test;
import pro.panopticon.client.model.Measurement;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SuccessrateSensorTest {

    @Test
    public void should_not_warn_before_reaching_enough_data() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        sensor.tickSuccess("key1");
        sensor.tickFailure("key1");
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(1));
        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("INFO"));
        assertThat(key1.get().displayValue, is("Last 2 calls: 1 success, 1 failure (50.00% failure) - not enough calls to report status yet"));
        assertThat(key1.get().numericValue, is(50L));
    }

    @Test
    public void should_get_status_info() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        IntStream.range(0, 100).forEach(i -> sensor.tickSuccess("key1"));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(1));
        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("INFO"));
        assertThat(key1.get().displayValue, is("Last 100 calls: 100 success, 0 failure (0.00% failure)"));
        assertThat(key1.get().numericValue, is(0L));
    }

    @Test
    public void should_get_status_warn() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        IntStream.range(0, 90).forEach(i -> sensor.tickSuccess("key1"));
        IntStream.range(0, 10).forEach(i -> sensor.tickFailure("key1"));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(1));
        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("WARN"));
        assertThat(key1.get().displayValue, is("Last 100 calls: 90 success, 10 failure (10.00% failure)"));
        assertThat(key1.get().numericValue, is(10L));
    }

    @Test
    public void should_get_status_error() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        IntStream.range(0, 80).forEach(i -> sensor.tickSuccess("key1"));
        IntStream.range(0, 20).forEach(i -> sensor.tickFailure("key1"));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(1));
        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("ERROR"));
        assertThat(key1.get().displayValue, is("Last 100 calls: 80 success, 20 failure (20.00% failure)"));
        assertThat(key1.get().numericValue, is(20L));
    }

    @Test
    public void should_get_status_info_when_warn_levels_is_null() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, null, null);
        IntStream.range(0, 100).forEach(i -> sensor.tickFailure("key1"));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(1));
        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("INFO"));
        assertThat(key1.get().displayValue, is("Last 100 calls: 0 success, 100 failure (100.00% failure)"));
        assertThat(key1.get().numericValue, is(100L));
    }

    @Test
    public void should_only_keep_last_XXX_measurements() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        IntStream.range(0, 100).forEach(i -> sensor.tickFailure("key1"));
        IntStream.range(0, 100).forEach(i -> sensor.tickSuccess("key1"));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(1));
        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("INFO"));
        assertThat(key1.get().displayValue, is("Last 100 calls: 100 success, 0 failure (0.00% failure)"));
        assertThat(key1.get().numericValue, is(0L));
    }

    @Test
    public void should_keep_data_for_multiple_keys() {
        SuccessrateSensor sensor = new SuccessrateSensor(100, 0.1, 0.2);
        IntStream.range(0, 50).forEach(i -> sensor.tickSuccess("key1"));
        IntStream.range(0, 50).forEach(i -> sensor.tickFailure("key1"));
        IntStream.range(0, 98).forEach(i -> sensor.tickSuccess("key2"));
        IntStream.range(0, 2).forEach(i -> sensor.tickFailure("key2"));
        List<Measurement> measurements = sensor.measure();
        assertThat(measurements.size(), is(2));

        Optional<Measurement> key1 = measurements.stream().filter(m -> m.key.equals("key1")).findAny();
        assertThat(key1.isPresent(), is(true));
        assertThat(key1.get().status, is("ERROR"));
        assertThat(key1.get().displayValue, is("Last 100 calls: 50 success, 50 failure (50.00% failure)"));
        assertThat(key1.get().numericValue, is(50L));

        Optional<Measurement> key2 = measurements.stream().filter(m -> m.key.equals("key2")).findAny();
        assertThat(key2.isPresent(), is(true));
        assertThat(key2.get().status, is("INFO"));
        assertThat(key2.get().displayValue, is("Last 100 calls: 98 success, 2 failure (2.00% failure)"));
        assertThat(key2.get().numericValue, is(2L));
    }

}
