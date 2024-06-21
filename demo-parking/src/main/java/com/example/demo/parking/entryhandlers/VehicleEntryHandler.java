package com.example.demo.parking.entryhandlers;

import com.example.demo.parking.vehicles.Vehicle;

public interface VehicleEntryHandler extends PlaceMatcher {

    boolean accept(Vehicle vehicle);
}
