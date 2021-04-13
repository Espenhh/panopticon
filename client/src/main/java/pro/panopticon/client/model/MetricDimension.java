package pro.panopticon.client.model;

public class MetricDimension {
    public final String name;
    public final String value;

    private static final String PLATFORM_DIMENSION_NAME = "Platform";
    private static final String HOST_DIMENSION_NAME = "Platform";

    private MetricDimension(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static MetricDimension platformDimension(String value) {
        return new MetricDimension(PLATFORM_DIMENSION_NAME, value);
    }

    public static MetricDimension hostDimension(String value) {
        return new MetricDimension(HOST_DIMENSION_NAME, value);
    }

    public static MetricDimension customDimension(String key, String value) {
        return new MetricDimension(key, value);
    }
}
