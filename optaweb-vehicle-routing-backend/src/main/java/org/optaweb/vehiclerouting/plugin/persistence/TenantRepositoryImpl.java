package org.optaweb.vehiclerouting.plugin.persistence;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.optaweb.vehiclerouting.domain.Tenant;
import org.optaweb.vehiclerouting.service.tenant.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class TenantRepositoryImpl implements TenantRepository {
    private static final Logger logger = LoggerFactory.getLogger(TenantRepositoryImpl.class);
    private final TenantCrudRepository repository;

    public TenantRepositoryImpl(TenantCrudRepository repository) {
        this.repository = repository;
    }

    private static Tenant toDomain(TenantEntity entity) {
        return new Tenant(
                entity.getId(),
                entity.getName(),
                entity.getDescription());
    }

    @Override
    public Tenant createTenant(String name, String description) {
        TenantEntity entity = repository.save(new TenantEntity(0, name, description));
        Tenant tenant = toDomain(entity);
        logger.info("Created tenant {}.", tenant);
        return tenant;
    }

    @Override
    public List<Tenant> tenants() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .map(TenantRepositoryImpl::toDomain)
                .collect(toList());
    }

    @Override
    public Tenant removeTenant(int id) {
        Optional<TenantEntity> maybeTenant = repository.findById(id);
        maybeTenant.ifPresent(tenantEntity -> repository.deleteById(id));
        TenantEntity tenantEntity = maybeTenant.orElseThrow(
                () -> new IllegalArgumentException("Tenant{id=" + id + "} doesn't exist"));
        Tenant tenant = toDomain(tenantEntity);
        logger.info("Deleted tenant {}.", tenant.fullDescription());
        return tenant;

    }

    @Override
    public Tenant updateTenant(int id, String name, String description) {
        Optional<TenantEntity> maybeTenant = repository.findById(id);
        if (!maybeTenant.isPresent()) {
            throw new IllegalArgumentException("Tenant{id=" + id + "} doesn't exist");
        }
        TenantEntity tenantEntity = repository.save(new TenantEntity(
                id,
                name,
                description));
        Tenant tenant = toDomain(tenantEntity);
        logger.info("Updated tenant {}.", tenant.fullDescription());
        return tenant;
    }

    @Override
    public Optional<Tenant> find(int id) {
        return repository.findById(id).map(TenantRepositoryImpl::toDomain);
    }
}
