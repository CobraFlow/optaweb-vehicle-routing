package org.optaweb.vehiclerouting.service.lifecycle;

import org.optaweb.vehiclerouting.plugin.planner.RLHSolverControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Closes down planner when user disconnects.
 */
@Service
public class PlannerLifecycleService {
    private static final Logger logger = LoggerFactory.getLogger(PlannerLifecycleService.class);

    private final RLHSolverControl solverControl;

    public PlannerLifecycleService(RLHSolverControl solverControl) {
        this.solverControl = solverControl;
    }

    @EventListener
    public void sessionDisconnectionHandler(SessionDisconnectEvent event) {
        logger.info("Disconnecting " + event);
        String problemId = event.getSessionId();
        solverControl.stopSolver(problemId);
    }
}
