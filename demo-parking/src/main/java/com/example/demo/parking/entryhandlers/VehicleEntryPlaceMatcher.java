package com.example.demo.parking.entryhandlers;

import com.example.demo.parking.Place;
import com.example.demo.parking.vehicles.Vehicle;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class VehicleEntryPlaceMatcher {

    private final List<? extends VehicleEntryHandler> handlers;

    public VehicleEntryPlaceMatcher(Place place) {
        requireNonNull(place);
        this.handlers = List.of(new DefaultVehicleEntryHandler(place), new VehicleDimensionsEntryHandler(place));
    }

    public double computeRankFor(Vehicle vehicle) {
        return handlers.stream()
                .filter(handler -> handler.accept(vehicle))
                .map(handler -> handler.computeRank(vehicle))
                .reduce(1d, (r1, r2) -> r1 * r2);
    }
}
