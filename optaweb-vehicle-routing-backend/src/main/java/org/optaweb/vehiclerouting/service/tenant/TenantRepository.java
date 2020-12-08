package org.optaweb.vehiclerouting.service.tenant;

import java.util.List;
import java.util.Optional;

import org.optaweb.vehiclerouting.domain.Tenant;

/**
 * Defines repository operations on tenants.
 */
public interface TenantRepository {

    /**
     * Create a tenant with a unique ID.
     *
     * @param name tenant's name
     * @param description long tenant description
     * @return a new tenant
     */
    Tenant createTenant(String name, String description);

    /**
     * Get all tenants.
     *
     * @return all tenants
     */
    List<Tenant> tenants();

    /**
     * Remove a tenant with the given ID.
     *
     * @param id tenant's ID.
     * @return the removed tenant
     */
    Tenant removeTenant(int id);

    /**
     * Update the tenant with the given ID.
     *
     * @param id tenant's id
     * @param name to update
     * @param description to update
     * @return the updated tenant
     */
    Tenant updateTenant(int id, String name, String description);

    /**
     * Find tenant by its ID.
     *
     * @param id tenant's ID
     * @return an Optional containing tenant with the given ID or empty Optional if there is no tenant with such ID
     */
    Optional<Tenant> find(int id);
}
