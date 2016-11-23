package no.panopticon.client.model;

public class ComponentInfo {

    public String environment;
    public String system;
    public String component;
    public String server;

    public ComponentInfo(String environment, String system, String component, String server) {
        this.environment = environment;
        this.system = system;
        this.component = component;
        this.server = server;
    }

    @Override
    public String toString() {
        return "ComponentInfo{" +
                "environment='" + environment + '\'' +
                ", system='" + system + '\'' +
                ", component='" + component + '\'' +
                ", server='" + server + '\'' +
                '}';
    }

}

