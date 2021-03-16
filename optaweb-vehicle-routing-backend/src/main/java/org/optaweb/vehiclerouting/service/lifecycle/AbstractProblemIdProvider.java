package org.optaweb.vehiclerouting.service.lifecycle;

import static org.springframework.messaging.simp.SimpAttributesContextHolder.getAttributes;

import org.springframework.messaging.simp.SimpAttributes;

public abstract class AbstractProblemIdProvider {

    public static String getProblemId() {
        //        return "" + TenantContext.getCurrentTenantId();
        SimpAttributes attributes = getAttributes();
        return "" + (attributes == null ? "" : attributes.getSessionId());
    }
}
