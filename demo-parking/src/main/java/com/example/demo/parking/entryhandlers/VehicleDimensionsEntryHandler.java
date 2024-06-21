package com.example.demo.parking.entryhandlers;

import com.example.demo.parking.Dimensions;
import com.example.demo.parking.Place;
import com.example.demo.parking.vehicles.Vehicle;

class VehicleDimensionsEntryHandler implements VehicleEntryHandler {

    private final Place place;

    VehicleDimensionsEntryHandler(Place place) {
        this.place = place;
    }

    @Override
    public boolean accept(Vehicle vehicle) {
        return true;
    }

    @Override
    public double computeRank(Vehicle vehicle) {
        Dimensions vehicleDimensions = vehicle.getDimensions();
        boolean contains = place.getDimensions().contains(vehicleDimensions);
        if(!contains) {
            return 0;
        }
        double lengthDiff = place.getDimensions().length() - vehicleDimensions.length();
        double widthDiff = place.getDimensions().width() - vehicleDimensions.width();
        double heighthDiff = place.getDimensions().height() - vehicleDimensions.height();
        return (place.getDimensions().length() + place.getDimensions().width() + place.getDimensions().height()) / (1 + lengthDiff + widthDiff + heighthDiff);
    }
}
