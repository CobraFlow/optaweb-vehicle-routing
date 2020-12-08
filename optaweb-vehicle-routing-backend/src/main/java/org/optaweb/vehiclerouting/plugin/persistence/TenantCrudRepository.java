package org.optaweb.vehiclerouting.plugin.persistence;

import org.springframework.data.repository.CrudRepository;

public interface TenantCrudRepository extends CrudRepository<TenantEntity, Integer> {
}
