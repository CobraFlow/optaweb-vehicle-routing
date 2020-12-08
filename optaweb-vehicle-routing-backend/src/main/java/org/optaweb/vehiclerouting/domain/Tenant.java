package org.optaweb.vehiclerouting.domain;

/**
 * Tenant that owns the data.
 */
public class Tenant extends TenantData {
    private final long id;

    public Tenant(long id, String name, String description) {
        super(name, description);
        this.id = id;
    }

    /**
     * Tenant's ID.
     *
     * @return unique ID
     */
    public long id() {
        return id;
    }

    /**
     * Full description of the tenant including its ID, name and description.
     *
     * @return full description
     */
    public String fullDescription() {
        return "[" + id + "]: " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Tenant vehicle = (Tenant) o;
        return id == vehicle.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return name().isEmpty() ? Long.toString(id) : (id + ": '" + name() + "'");
    }
}
