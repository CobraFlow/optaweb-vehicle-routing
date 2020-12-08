package org.optaweb.vehiclerouting.service.tenant;

import org.optaweb.vehiclerouting.plugin.persistence.TenantEntity;

public class TenantContext {

    private static final ThreadLocal<TenantEntity> currentTenant = new InheritableThreadLocal<>();
    private static final ThreadLocal<Integer> currentTenantId = new InheritableThreadLocal<>();

    public static TenantEntity getCurrentTenant() {
        return currentTenant.get();
    }

    public static void setCurrentTenant(TenantEntity tenant) {
        currentTenant.set(tenant);
        currentTenantId.set(tenant.getId());
    }

    public static Integer getCurrentTenantId() {
        return currentTenantId.get();
    }

    public static void clear() {
        currentTenant.set(null);
    }
}