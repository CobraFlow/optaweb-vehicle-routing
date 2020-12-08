package org.optaweb.vehiclerouting.domain;

import java.util.Objects;

/**
 * Data about a tenant
 */
public class TenantData {

    private final String name;
    private final String description;

    public TenantData(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Tenant's name (unique description).
     *
     * @return tenant's name
     */
    public String name() {
        return name;
    }

    /**
     * Tenant's description (long description).
     *
     * @return tenant's description
     */
    public String description() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TenantData that = (TenantData) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description);
    }

    @Override
    public String toString() {
        return name.isEmpty() ? "<noname>" : "'" + name + "' " + (description.isEmpty() ? "<nodesc>" : "'" + description + "'");
    }
}
