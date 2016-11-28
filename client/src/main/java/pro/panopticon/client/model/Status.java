package pro.panopticon.client.model;

import java.util.List;

public class Status {

    public String environment;
    public String system;
    public String component;
    public String server;
    public List<Measurement> measurements;

    public Status(String environment, String system, String component, String server, List<Measurement> measurements) {
        this.environment = environment;
        this.system = system;
        this.component = component;
        this.server = server;
        this.measurements = measurements;
    }

    public Status(ComponentInfo componentInfo, List<Measurement> measurements) {
        this(componentInfo.environment, componentInfo.system, componentInfo.component, componentInfo.server, measurements);
    }


    @Override
    public String toString() {
        return "UpdatedStatus{" +
                "environment='" + environment + '\'' +
                ", system='" + system + '\'' +
                ", component='" + component + '\'' +
                ", server='" + server + '\'' +
                ", measurements=" + measurements +
                '}';
    }

}

