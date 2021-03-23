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

package org.optaweb.vehiclerouting.service.vehicle;

import static java.util.Comparator.comparingLong;

import java.util.Objects;
import java.util.Optional;

import org.optaweb.vehiclerouting.domain.Vehicle;
import org.optaweb.vehiclerouting.domain.VehicleData;
import org.optaweb.vehiclerouting.domain.VehicleFactory;
import org.optaweb.vehiclerouting.service.error.ErrorEvent;
import org.optaweb.vehiclerouting.service.location.RouteOptimizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VehicleService {

    static final int DEFAULT_VEHICLE_CAPACITY = 10;
    private final VehicleRepository vehicleRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RouteOptimizer optimizer;

    @Autowired
    public VehicleService(
            RouteOptimizer optimizer,
            VehicleRepository vehicleRepository,
            ApplicationEventPublisher eventPublisher) {
        this.optimizer = optimizer;
        this.vehicleRepository = vehicleRepository;
        this.eventPublisher = eventPublisher;
    }

    public void createVehicle() {
        Vehicle vehicle = vehicleRepository.createVehicle(DEFAULT_VEHICLE_CAPACITY);
        addVehicle(vehicle);
    }

    public void createVehicle(VehicleData vehicleData) {
        Vehicle vehicle = vehicleRepository.createVehicle(vehicleData);
        addVehicle(vehicle);
    }

    public void addVehicle(Vehicle vehicle) {
        optimizer.addVehicle(Objects.requireNonNull(vehicle));
    }

    public void removeVehicle(long vehicleId) {
        Vehicle vehicle = vehicleRepository.removeVehicle(vehicleId);
        optimizer.removeVehicle(vehicle);
    }

    public void updateVehicle(long id, String name) {
        Optional<Vehicle> optionalVehicle = vehicleRepository.find(id);
        if (!optionalVehicle.isPresent()) {
            eventPublisher.publishEvent(
                    new ErrorEvent(this, "Vehicle [" + id + "] cannot be updated because it doesn't exist."));
            return;
        }
        Vehicle vehicle = optionalVehicle.get();
        vehicle = new Vehicle(
                vehicle.id(),
                name,
                vehicle.capacity());
        vehicleRepository.update(vehicle);
        optimizer.nopChange();
    }

    public synchronized void removeAnyVehicle() {
        Optional<Vehicle> first = vehicleRepository.vehicles().stream().min(comparingLong(Vehicle::id));
        first.ifPresent(vehicle -> {
            Vehicle removed = vehicleRepository.removeVehicle(vehicle.id());
            optimizer.removeVehicle(removed);
        });
    }

    public void removeAll() {
        optimizer.removeAllVehicles();
        vehicleRepository.removeAll();
    }

    public void changeCapacity(long vehicleId, int capacity) {
        Vehicle vehicle = vehicleRepository.find(vehicleId).orElseThrow(() -> new IllegalArgumentException(
                "Can't remove Vehicle{id=" + vehicleId + "} because it doesn't exist"));
        Vehicle updatedVehicle = VehicleFactory.createVehicle(vehicle.id(), vehicle.name(), capacity);
        vehicleRepository.update(updatedVehicle);
        optimizer.changeCapacity(updatedVehicle);
    }
}
