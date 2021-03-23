package org.optaweb.vehiclerouting.plugin.security.aop;

import org.optaweb.vehiclerouting.plugin.persistence.TenantEntity;

public class TenantContext {

    private static ThreadLocal<TenantEntity> currentTenant = new InheritableThreadLocal<>();
    private static ThreadLocal<Integer> currentTenantId = new InheritableThreadLocal<>();

    public static TenantEntity getCurrentTenant() {
        return currentTenant.get();
    }

    public static Integer getCurrentTenantId() {
        Integer id = currentTenantId.get();
        return id == null ? 0 : id;
    }

    public static void setCurrentTenant(TenantEntity tenant) {
        currentTenant.set(tenant);
        currentTenantId.set(tenant.getId());
    }

    public static void clear() {
        currentTenant.set(null);
    }
}
