package no.panopticon.api.external.sns;

public class Dimension {

    public String name;
    public String value;

    @Override
    public String toString() {
        return "Dimension{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
