/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import static org.optaweb.vehiclerouting.service.lifecycle.AbstractProblemIdProvider.getProblemId;

import java.util.ArrayList;
import java.util.List;

import org.optaweb.vehiclerouting.domain.Location;
import org.optaweb.vehiclerouting.domain.Vehicle;
import org.optaweb.vehiclerouting.plugin.planner.domain.*;
import org.optaweb.vehiclerouting.service.location.DistanceMatrixRow;
import org.optaweb.vehiclerouting.service.location.RouteOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * Accumulates vehicles, depots and visits until there's enough data to start the optimization.
 * Solutions are published even if solving hasn't started yet due to missing facts (e.g. no vehicles or no visits).
 * Stops solver when vehicles or visits are reduced to zero.
 */
@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.INTERFACES)
class RLHRouteOptimizer implements RouteOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(RLHRouteOptimizer.class);

    private final RLHSolverControl solverControl;
    private final RouteChangedEventPublisher routeChangedEventPublisher;

    private final List<PlanningVehicle> vehicles = new ArrayList<>();
    private final List<PlanningVisit> visits = new ArrayList<>();
    private PlanningDepot depot;

    @Autowired
    RLHRouteOptimizer(RLHSolverControl solverControl,
            RouteChangedEventPublisher routeChangedEventPublisher) {
        this.solverControl = solverControl;
        this.routeChangedEventPublisher = routeChangedEventPublisher;
        logger.info("Created new RLHRouteOptimizer");
    }

    @Override
    public void addLocation(Location domainLocation, DistanceMatrixRow distanceMatrixRow) {
        PlanningLocation location = PlanningLocationFactory.fromDomain(
                domainLocation,
                new DistanceMapImpl(distanceMatrixRow));
        // Unfortunately can't start solver with an empty solution (see https://issues.redhat.com/browse/PLANNER-776)
        if (depot == null) {
            depot = new PlanningDepot(location);
            publishSolution();
        } else {
            String problemId = getProblemId();
            PlanningVisit visit = PlanningVisitFactory.fromLocation(location);
            visits.add(visit);
            if (vehicles.isEmpty()) {
                publishSolution();
            } else if (visits.size() == 1) {
                solverControl.startSolver(problemId, SolutionFactory.solutionFromVisits(vehicles, depot, visits));
            } else {
                solverControl.addVisit(problemId, visit);
            }
        }
    }

    @Override
    public void removeLocation(Location domainLocation) {
        if (visits.isEmpty()) {
            if (depot == null) {
                throw new IllegalArgumentException(
                        "Cannot remove " + domainLocation + " because there are no locations");
            }
            if (depot.getId() != domainLocation.id()) {
                throw new IllegalArgumentException("Cannot remove " + domainLocation + " because it doesn't exist");
            }
            depot = null;
            publishSolution();
        } else {
            String problemId = getProblemId();
            if (depot.getId() == domainLocation.id()) {
                throw new IllegalStateException("You can only remove depot if there are no visits");
            }
            if (!visits.removeIf(item -> item.getId() == domainLocation.id())) {
                throw new IllegalArgumentException("Cannot remove " + domainLocation + " because it doesn't exist");
            }
            if (vehicles.isEmpty()) { // solver is not running
                publishSolution();
            } else if (visits.isEmpty()) { // solver is running
                solverControl.stopSolver(problemId);
                publishSolution();
            } else {
                solverControl.removeVisit(
                        problemId,
                        PlanningVisitFactory.fromLocation(PlanningLocationFactory.fromDomain(domainLocation)));
            }
        }
    }

    @Override
    public void addVehicle(Vehicle domainVehicle) {
        String problemId = getProblemId();
        PlanningVehicle vehicle = PlanningVehicleFactory.fromDomain(domainVehicle);
        vehicle.setDepot(depot);
        vehicles.add(vehicle);
        if (visits.isEmpty()) {
            publishSolution();
        } else if (vehicles.size() == 1) {
            solverControl.startSolver(problemId, SolutionFactory.solutionFromVisits(vehicles, depot, visits));
        } else {
            solverControl.addVehicle(problemId, vehicle);
        }
    }

    @Override
    public void removeVehicle(Vehicle domainVehicle) {
        if (!vehicles.removeIf(vehicle -> vehicle.getId() == domainVehicle.id())) {
            throw new IllegalArgumentException("Cannot remove " + domainVehicle + " because it doesn't exist");
        }
        String problemId = getProblemId();
        if (visits.isEmpty()) { // solver is not running
            publishSolution();
        } else if (vehicles.isEmpty()) { // solver is running
            solverControl.stopSolver(problemId);
            publishSolution();
        } else {
            solverControl.removeVehicle(problemId, PlanningVehicleFactory.fromDomain(domainVehicle));
        }
    }

    @Override
    public void changeCapacity(Vehicle domainVehicle) {
        PlanningVehicle vehicle = vehicles.stream()
                .filter(item -> item.getId() == domainVehicle.id())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot change capacity of " + domainVehicle + " because it doesn't exist"));
        vehicle.setCapacity(domainVehicle.capacity());
        if (!visits.isEmpty()) {
            solverControl.changeCapacity(getProblemId(), vehicle);
        } else {
            publishSolution();
        }
    }

    @Override
    public void nopChange() {
        solverControl.nopChange(getProblemId());
        publishSolution();
    }

    @Override
    public void removeAllLocations() {
        solverControl.stopSolver(getProblemId());
        depot = null;
        visits.clear();
        publishSolution();
    }

    @Override
    public void removeAllVehicles() {
        solverControl.stopSolver(getProblemId());
        vehicles.clear();
        publishSolution();
    }

    private void publishSolution() {
        routeChangedEventPublisher.publishSolution(SolutionFactory.solutionFromVisits(vehicles, depot, visits));
    }
}
