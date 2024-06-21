package com.example.demo.parking.vehicles;


import com.example.demo.parking.Dimensions;
import com.example.demo.parking.Place;

import java.util.Optional;
import java.util.UUID;

public abstract class Vehicle {

    private final String id;
    private final Dimensions dimensions;
    private Place place;

    protected Vehicle(Dimensions dimensions) {
        this.dimensions = dimensions;
        this.id = UUID.randomUUID().toString();
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public abstract String getType();

    public Optional<Place> getPlace() {
        return Optional.ofNullable(this.place);
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                "type='" + getType() + '\'' +
                ", dimensions=" + dimensions +
                '}';
    }
}
