package org.optaweb.vehiclerouting.plugin.planner;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;

import org.optaplanner.core.api.solver.ProblemFactChange;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.core.impl.solver.DefaultSolverJob;
import org.optaplanner.core.impl.solver.DefaultSolverManager;
import org.optaweb.vehiclerouting.plugin.planner.change.*;
import org.optaweb.vehiclerouting.plugin.planner.domain.PlanningVehicle;
import org.optaweb.vehiclerouting.plugin.planner.domain.PlanningVisit;
import org.optaweb.vehiclerouting.plugin.planner.domain.VehicleRoutingSolution;
import org.springframework.stereotype.Service;

@Service
public class RLHSolverControl implements SolverEventListener<VehicleRoutingSolution> {
    private final SolverManager<VehicleRoutingSolution, String> solverManager;
    private final RouteChangedEventPublisher routeChangedEventPublisher;

    public RLHSolverControl(SolverManager<VehicleRoutingSolution, String> solverManager,
            RouteChangedEventPublisher routeChangedEventPublisher) {
        this.solverManager = solverManager;
        this.routeChangedEventPublisher = routeChangedEventPublisher;
    }

    void startSolver(String problemId, VehicleRoutingSolution solution) {
        SolverJob<VehicleRoutingSolution, String> solverJob = getSolverJob(problemId);
        if (solverJob != null) {
            return;
            //            throw new IllegalStateException("Solver start has already been requested");
        }

        solverJob = solverManager.solveAndListen(
                problemId,
                s -> solution,
                routeChangedEventPublisher::publishSolution);
        /*
         * Authentication p = SecurityContextHolder.getContext().getAuthentication();
         * solverFuture = executor.submitListenable((SolverManagerX.SolvingTask) () -> {
         * SecurityContextHolder.getContext().setAuthentication(p);
         * return solver.solve(solution);
         * });
         * solverFuture.addCallback(
         * // IMPORTANT: This is happening on the solver thread.
         * // TODO in both cases restart or somehow recover?
         * result -> {
         * if (!solver.isTerminateEarly()) {
         * // This is impossible. Solver in daemon mode can't return from solve() unless it has been
         * // terminated (see #stopSolver()) or throws an exception.
         * logger.error("Solver stopped solving but that shouldn't happen in daemon mode.");
         * eventPublisher.publishEvent(new ErrorEvent(this, "Solver stopped solving unexpectedly."));
         * }
         * },
         * exception -> {
         * logger.error("Solver failed", exception);
         * eventPublisher.publishEvent(new ErrorEvent(this, exception.toString()));
         * });
         */
    }

    public void stopSolver(String problemId) {
        SolverJob<VehicleRoutingSolution, String> solverJob = getSolverJob(problemId);
        if (solverJob != null) {
            // TODO what happens if solver hasn't started yet (solve() is called asynchronously)
            solverJob.terminateEarly();
            // make sure solver has terminated and propagate exceptions
            /*
             * try {
             * solverFuture.get();
             * solverFuture = null;
             * } catch (InterruptedException e) {
             * Thread.currentThread().interrupt();
             * throw new RuntimeException("Failed to stop solver", e);
             * } catch (ExecutionException e) {
             * // Skipping the wrapper ExecutionException because it only tells that the problem occurred
             * // in solverFuture.get() but that's obvious.
             * throw new RuntimeException("Failed to stop solver", e.getCause());
             * }
             */
            solverJob.getSolverStatus();
        }
    }

    private void assertSolverIsAlive(String problemId) {
        SolverJob<VehicleRoutingSolution, String> solverJob = getSolverJob(problemId);
        if (solverJob == null) {
            throw new IllegalStateException("Solver has not started yet");
        }
        if (solverJob.getSolverStatus() == SolverStatus.NOT_SOLVING)
            throw new IllegalStateException("Solver has finished solving even though it operates in daemon mode");
        /*
         * if (solverFuture.isDone()) {
         * try {
         * solverFuture.get();
         * } catch (InterruptedException e) {
         * Thread.currentThread().interrupt();
         * throw new RuntimeException("Solver has died", e);
         * } catch (ExecutionException e) {
         * // Skipping the wrapper ExecutionException because it only tells that the problem occurred
         * // in solverFuture.get() but that's obvious.
         * throw new RuntimeException("Solver has died", e.getCause());
         * }
         * throw new IllegalStateException("Solver has finished solving even though it operates in daemon mode");
         * }
         */
    }

    void addVisit(String problemId, PlanningVisit visit) {
        assertSolverIsAlive(problemId);
        addProblemFactChange(problemId, new AddVisit(visit));
    }

    void removeVisit(String problemId, PlanningVisit visit) {
        assertSolverIsAlive(problemId);
        addProblemFactChange(problemId, new RemoveVisit(visit));
    }

    void addVehicle(String problemId, PlanningVehicle vehicle) {
        assertSolverIsAlive(problemId);
        addProblemFactChange(problemId, new AddVehicle(vehicle));
    }

    void removeVehicle(String problemId, PlanningVehicle vehicle) {
        assertSolverIsAlive(problemId);
        addProblemFactChange(problemId, new RemoveVehicle(vehicle));
    }

    void changeCapacity(String problemId, PlanningVehicle vehicle) {
        assertSolverIsAlive(problemId);
        addProblemFactChange(problemId, new ChangeVehicleCapacity(vehicle));
    }

    void nopChange(String problemId) {
        // Don't want/need to check that the solver is alive.
        //        assertSolverIsAlive();
        addProblemFactChange(problemId, new NopChange());
    }

    protected SolverJob<VehicleRoutingSolution, String> getSolverJob(String problemId) {
        try {
            Method method = DefaultSolverManager.class.getDeclaredMethod("getProblemIdToSolverJobMap");
            method.setAccessible(true);
            ConcurrentMap<Object, DefaultSolverJob<VehicleRoutingSolution, String>> jobs =
                    (ConcurrentMap<Object, DefaultSolverJob<VehicleRoutingSolution, String>>) method.invoke(solverManager);
            return jobs.get(problemId);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addProblemFactChange(String problemId, ProblemFactChange<VehicleRoutingSolution> change) {
        try {
            SolverJob<VehicleRoutingSolution, String> solverJob = getSolverJob(problemId);
            Field field = DefaultSolverJob.class.getDeclaredField("solver");
            field.setAccessible(true);
            DefaultSolver<VehicleRoutingSolution> solver = (DefaultSolver<VehicleRoutingSolution>) field.get(solverJob);
            solver.addProblemFactChange(change);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void bestSolutionChanged(BestSolutionChangedEvent<VehicleRoutingSolution> event) {
        System.out.println(event);
    }

    /**
     * An alias interface that fixates the Callable's type parameter. This avoids unchecked warnings in tests.
     */
    interface SolvingTask extends Callable<VehicleRoutingSolution> {

    }
}
