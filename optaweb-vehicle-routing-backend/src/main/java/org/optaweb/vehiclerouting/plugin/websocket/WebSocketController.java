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

package org.optaweb.vehiclerouting.plugin.websocket;

import org.optaweb.vehiclerouting.domain.Coordinates;
import org.optaweb.vehiclerouting.domain.RoutingPlan;
import org.optaweb.vehiclerouting.domain.TenantData;
import org.optaweb.vehiclerouting.service.demo.DemoService;
import org.optaweb.vehiclerouting.service.error.ErrorEvent;
import org.optaweb.vehiclerouting.service.location.LocationService;
import org.optaweb.vehiclerouting.service.location.RouteOptimizer;
import org.optaweb.vehiclerouting.service.region.BoundingBox;
import org.optaweb.vehiclerouting.service.region.RegionService;
import org.optaweb.vehiclerouting.service.route.RouteListener;
import org.optaweb.vehiclerouting.service.tenant.TenantService;
import org.optaweb.vehiclerouting.service.vehicle.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Handles WebSocket subscriptions and STOMP messages.
 *
 * @see WebSocketConfig
 */
@Controller
class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    private final RouteListener routeListener;
    private final RegionService regionService;
    private final LocationService locationService;
    private final VehicleService vehicleService;
    private final TenantService tenantService;
    private final DemoService demoService;
    private final ApplicationEventPublisher eventPublisher;
    private final RouteOptimizer optimizer;

    @Autowired
    WebSocketController(
            RouteListener routeListener,
            RegionService regionService,
            LocationService locationService,
            VehicleService vehicleService,
            TenantService tenantService,
            DemoService demoService,
            ApplicationEventPublisher eventPublisher,
            RouteOptimizer optimizer) {
        this.routeListener = routeListener;
        this.regionService = regionService;
        this.locationService = locationService;
        this.vehicleService = vehicleService;
        this.tenantService = tenantService;
        this.demoService = demoService;
        this.eventPublisher = eventPublisher;
        this.optimizer = optimizer;
    }

    @MessageExceptionHandler
    void handleException(Exception exception) {
        logger.error("Uncaught exception", exception);
        eventPublisher.publishEvent(new ErrorEvent(this, exception.toString()));
    }

    /**
     * Subscribe to server info topic.
     *
     * @return server info
     */
    @SubscribeMapping("/serverInfo")
    ServerInfo subscribeToServerInfoTopic() {
        BoundingBox boundingBox = regionService.boundingBox();
        List<PortableCoordinates> portableBoundingBox = Arrays.asList(
                PortableCoordinates.fromCoordinates(boundingBox.getSouthWest()),
                PortableCoordinates.fromCoordinates(boundingBox.getNorthEast()));
        List<RoutingProblemInfo> demos = demoService.demos().stream()
                .map(routingProblem -> new RoutingProblemInfo(
                        routingProblem.name(),
                        routingProblem.visits().size()))
                .collect(toList());
        return new ServerInfo(portableBoundingBox, regionService.countryCodes(), demos);
    }

    /**
     * Subscribe for updates of the VRP route.
     *
     * @return route message
     */
    @SubscribeMapping("/route")
    PortableRoutingPlan subscribeToRouteTopic() {
        RoutingPlan routingPlan = routeListener.getBestRoutingPlan();
        return PortableRoutingPlanFactory.fromRoutingPlan(routingPlan);
    }

    /**
     * Create new tenant.
     *
     * @param request new tenant description
     */
    @MessageMapping("/tenant")
    void addTenant(PortableTenant request) {
        tenantService.createTenant(new TenantData(
                request.getName(),
                request.getDescription()));
    }

    /**
     * Delete tenant.
     *
     * @param id ID of the tenant to be deleted
     */
    @MessageMapping("/tenant/{id}/delete")
    void removeTenant(@DestinationVariable int id) {
        tenantService.removeTenant(id);
    }

    /**
     * Update tenant.
     *
     * @param id      ID of the tenant to be updated
     * @param request updated tenant
     */
    @MessageMapping("/tenant/{id}")
    void updateTenant(@DestinationVariable int id, PortableTenant request) {
        tenantService.updateTenant(
                id,
                request.getName(),
                request.getDescription());
    }

    /**
     * Create new location.
     *
     * @param request new location description
     */
    @MessageMapping("/location")
    void addLocation(PortableLocation request) {
        locationService.createLocation(
                new Coordinates(request.getLatitude(), request.getLongitude()),
                request.getDescription());
    }

    /**
     * Delete location.
     *
     * @param id ID of the location to be deleted
     */
    @MessageMapping("/location/{id}/delete")
    void removeLocation(@DestinationVariable long id) {
        locationService.removeLocation(id);
    }

    /**
     * Update location.
     *
     * @param id      ID of the location to be updated
     * @param request updated location
     */
    @MessageMapping("/location/{id}")
    void updateLocation(@DestinationVariable long id, PortableLocation request) {
        locationService.updateLocation(id, request.getDescription());
        optimizer.nopChange();
    }

    /**
     * Load a demo data set.
     *
     * @param name data set name
     */
    @MessageMapping("/demo/{name}")
    void demo(@DestinationVariable String name) {
        demoService.loadDemo(name);
    }

    @MessageMapping("/clear")
    void clear() {
        // TODO do this in one step (=> new RoutingPlanService)
        locationService.removeAll();
        vehicleService.removeAll();
    }

    @MessageMapping("/vehicle")
    void addVehicle() {
        vehicleService.createVehicle();
    }

    /**
     * Delete vehicle.
     *
     * @param id ID of the vehicle to be deleted
     */
    @MessageMapping("/vehicle/{id}/delete")
    void removeVehicle(@DestinationVariable long id) {
        vehicleService.removeVehicle(id);
    }

    /**
     * Update vehicle.
     *
     * @param id      ID of the location to be updated
     * @param request updated location
     */
    @MessageMapping("/vehicle/{id}")
    void updateVehicle(@DestinationVariable long id, PortableVehicle request) {
        vehicleService.updateVehicle(id, request.getName());
        optimizer.nopChange();
    }

    @MessageMapping("/vehicle/deleteAny")
    void removeAnyVehicle() {
        vehicleService.removeAnyVehicle();
    }

    @MessageMapping("/vehicle/{id}/capacity")
    void changeCapacity(@DestinationVariable long id, int capacity) {
        vehicleService.changeCapacity(id, capacity);
    }
}
