package org.optaweb.vehiclerouting.plugin.persistence;

import javax.persistence.*;

@MappedSuperclass
public class Base {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private TenantEntity tenant;

    public Base(TenantEntity tenant) {
        this.tenant = tenant;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    public void setTenant(TenantEntity tenant) {
        this.tenant = tenant;
    }

    /*
     * @PrePersist
     * 
     * @PreUpdate
     * public void prePersist() {
     * setTenant(TenantContext.getCurrentTenant());
     * }
     */
}
