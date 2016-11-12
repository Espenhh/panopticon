package no.panopticon.api.storage;

import java.util.Objects;

public class RunningUnit {

    private final String environment;
    private final String system;
    private final String component;
    private final String server;

    public RunningUnit(String environment, String system, String component, String server) {
        this.environment = environment;
        this.system = system;
        this.component = component;
        this.server = server;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getSystem() {
        return system;
    }

    public String getComponent() {
        return component;
    }

    public String getServer() {
        return server;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunningUnit that = (RunningUnit) o;
        return Objects.equals(environment, that.environment) &&
                Objects.equals(system, that.system) &&
                Objects.equals(component, that.component) &&
                Objects.equals(server, that.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environment, system, component, server);
    }

    @Override
    public String toString() {
        return "RunningUnit{" +
                "environment='" + environment + '\'' +
                ", system='" + system + '\'' +
                ", component='" + component + '\'' +
                ", server='" + server + '\'' +
                '}';
    }
}
