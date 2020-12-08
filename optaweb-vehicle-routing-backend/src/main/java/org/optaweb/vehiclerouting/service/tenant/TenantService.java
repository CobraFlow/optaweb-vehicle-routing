package org.optaweb.vehiclerouting.service.tenant;

import org.optaweb.vehiclerouting.domain.TenantData;
import org.springframework.stereotype.Service;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public void createTenant(TenantData tenantData) {
        tenantRepository.createTenant(
                tenantData.name(),
                tenantData.description());
    }

    public void removeTenant(int id) {
        tenantRepository.removeTenant(id);
    }

    public void updateTenant(int id, String name, String description) {
        tenantRepository.updateTenant(id, name, description);
    }
}
