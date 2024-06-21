package com.example.demo.parking.entryhandlers;

import com.example.demo.parking.Place;
import com.example.demo.parking.vehicles.Vehicle;

class DefaultVehicleEntryHandler implements VehicleEntryHandler {

    private final Place place;

    DefaultVehicleEntryHandler(Place place) {
        this.place = place;
    }

    @Override
    public double computeRank(Vehicle vehicle) {
        if(!place.isFree()) {
            return 0;
        }
        if(!place.isAcceptedVehicleType(vehicle.getType())) {
            return 0;
        }
        int vehicleTypesCount = place.getAcceptedVehicleTypesCount();
        if(vehicleTypesCount == 0) {
            return 1;
        }
        return 1d + 1d / vehicleTypesCount;
    }

    @Override
    public boolean accept(Vehicle vehicle) {
        return true;
    }
}
