package com.example.demo.parking.vehicles;

import com.example.demo.parking.Dimensions;

public class Car extends ThermicVehicle {

    public Car(Dimensions dimensions) {
        super(dimensions);
    }

    @Override
    public String getType() {
        return "thermic-car";
    }
}
