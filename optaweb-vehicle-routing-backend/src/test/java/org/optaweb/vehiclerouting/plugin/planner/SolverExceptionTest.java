/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.vehiclerouting.plugin.planner;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaweb.vehiclerouting.plugin.planner.domain.*;
import org.optaweb.vehiclerouting.service.error.ErrorEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.util.concurrent.ListenableFutureTask;

@Disabled("RLH")
@ExtendWith(MockitoExtension.class)
class SolverExceptionTest {

    private static final String PROBLEM_ID = "PROBLEM_ID";
    @Mock
    private SolverManager<VehicleRoutingSolution, String> solverManager;
    @Mock
    private Solver<VehicleRoutingSolution> solver;
    @Mock
    private AsyncListenableTaskExecutor executor;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private RLHSolverControl solverControl;

    private static void assertTestExceptionThrownDuringOperation(ThrowingCallable runnable) {
        assertTestExceptionThrownDuring(runnable, "died");
    }

    private static void assertTestExceptionThrownWhenStoppingSolver(RLHSolverControl routeOptimizer) {
        assertTestExceptionThrownDuring(() -> routeOptimizer.stopSolver(PROBLEM_ID), "stop");
    }

    private static void assertTestExceptionThrownDuring(ThrowingCallable runnable, String message) {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(runnable)
                .withMessageContaining(message)
                .withCauseInstanceOf(TestException.class);
    }

    @Test
    void should_publish_error_if_solver_stops_solving_without_being_terminated() {
        // arrange
        when(solverControl.getSolverJob(anyString())).thenReturn(null);

        // Prepare a future that will be returned by mock executor
        ListenableFutureTask<VehicleRoutingSolution> task = new ListenableFutureTask<>(SolutionFactory::emptySolution);
        when(executor.submitListenable(any(RLHSolverControl.SolvingTask.class))).thenReturn(task);
        // Run it synchronously (otherwise the test would be unreliable!)
        task.run();

        // act
        solverControl.startSolver(PROBLEM_ID, SolutionFactory.emptySolution());

        // assert
        verify(eventPublisher).publishEvent(any(ErrorEvent.class));
    }

    @Test
    void should_not_publish_error_if_solver_is_terminated_early() {
        // arrange
        // Prepare a future that will be returned by mock executor
        ListenableFutureTask<VehicleRoutingSolution> task = new ListenableFutureTask<>(SolutionFactory::emptySolution);
        when(executor.submitListenable(any(RLHSolverControl.SolvingTask.class))).thenReturn(task);
        // Pretend the solver has been terminated by stopSolver()...
        when(solver.isTerminateEarly()).thenReturn(true);

        // act
        solverControl.startSolver(PROBLEM_ID, SolutionFactory.emptySolution());
        task.run(); // ...so that when this invokes the success callback, it won't publish an error

        // assert
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void should_propagate_any_exception_from_solver() {
        // arrange
        // Prepare a future that will be returned by mock executor
        ListenableFutureTask<VehicleRoutingSolution> task = new ListenableFutureTask<>(() -> {
            throw new TestException();
        });
        when(executor.submitListenable(any(RLHSolverControl.SolvingTask.class))).thenReturn(task);
        // act (1)
        // Run it synchronously (otherwise the test would be unreliable!)
        task.run();
        solverControl.startSolver(PROBLEM_ID, SolutionFactory.emptySolution());

        // assert (1)
        verify(eventPublisher).publishEvent(any(ErrorEvent.class));

        PlanningVisit planningVisit = PlanningVisitFactory.testVisit(1);
        PlanningVehicle planningVehicle = PlanningVehicleFactory.testVehicle(1);

        // act & assert (2)
        assertTestExceptionThrownDuringOperation(() -> solverControl.addVisit(PROBLEM_ID, planningVisit));
        assertTestExceptionThrownDuringOperation(() -> solverControl.removeVisit(PROBLEM_ID, planningVisit));
        assertTestExceptionThrownDuringOperation(() -> solverControl.addVehicle(PROBLEM_ID, planningVehicle));
        assertTestExceptionThrownDuringOperation(() -> solverControl.removeVehicle(PROBLEM_ID, planningVehicle));

        assertTestExceptionThrownWhenStoppingSolver(solverControl);
    }

    private static class TestException extends RuntimeException {

    }
}
