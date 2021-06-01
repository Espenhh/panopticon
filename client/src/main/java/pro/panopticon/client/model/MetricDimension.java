package pro.panopticon.client.model;

public class MetricDimension {
    public final String name;
    public final String value;

    private static final String PLATFORM_DIMENSION_NAME = "Platform";
    private static final String INSTANCE_DIMENSION_NAME = "Instance";

    private MetricDimension(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static MetricDimension platformDimension(String value) {
        return new MetricDimension(PLATFORM_DIMENSION_NAME, value);
    }

    public static MetricDimension instanceDimension(String value) {
        return new MetricDimension(INSTANCE_DIMENSION_NAME, value);
    }

    public static MetricDimension customDimension(String key, String value) {
        return new MetricDimension(key, value);
    }
}