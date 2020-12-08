package org.optaweb.vehiclerouting.plugin.websocket;

import java.util.Objects;

import org.optaweb.vehiclerouting.domain.Tenant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@link Tenant} representation suitable for network transport.
 */
public class PortableTenant {

    private final int id;
    private final String name;
    private final String description;

    @JsonCreator
    PortableTenant(
            @JsonProperty(value = "id") int id,
            @JsonProperty(value = "name") String name,
            @JsonProperty(value = "description") String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PortableTenant that = (PortableTenant) o;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }

    @Override
    public String toString() {
        return "PortableTenant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
