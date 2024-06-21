package com.example.demo.parking.entryhandlers;

import com.example.demo.parking.vehicles.Vehicle;

public interface PlaceMatcher {

    double computeRank(Vehicle vehicle);
}
