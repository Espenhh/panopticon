package pro.panopticon.client.sensor;

import pro.panopticon.client.model.Measurement;

import java.util.List;
import java.util.Objects;

@FunctionalInterface
public interface Sensor {

    List<Measurement> measure();

    class AlertInfo {

        /**
         * Key used to separate alerts from each other.
         * Example:
         * "entur.rest.calls"
         */
        private final String sensorKey;

        /**
         * A human / guard-friendly description of what is happening and which actions that needs to be taken.
         *
         * Example:
         * "When this alert is triggered, the critical Feature X is not working properly. You should contact Company Y."
         */
        private final String description;

        public AlertInfo(String sensorKey, String description) {
            this.sensorKey = sensorKey;
            this.description = description;
        }

        public String getSensorKey() {
            return sensorKey;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AlertInfo alertInfo = (AlertInfo) o;
            return Objects.equals(sensorKey, alertInfo.sensorKey) &&
                    Objects.equals(description, alertInfo.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sensorKey, description);
        }

        @Override
        public String toString() {
            return "AlertInfo{" +
                    "sensorKey='" + sensorKey + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

}
