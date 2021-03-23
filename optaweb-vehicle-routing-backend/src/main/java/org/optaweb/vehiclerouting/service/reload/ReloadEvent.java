package org.optaweb.vehiclerouting.service.reload;

import org.springframework.context.ApplicationEvent;

public class ReloadEvent extends ApplicationEvent {

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *        which the event is associated (never {@code null})
     */
    public ReloadEvent(Object source) {
        super(source);
    }
}
