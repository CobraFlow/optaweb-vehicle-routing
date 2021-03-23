package org.optaweb.vehiclerouting.plugin.persistence;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface TenantCrudRepository extends CrudRepository<TenantEntity, Integer> {
    Optional<TenantEntity> findByName(String name);
}
