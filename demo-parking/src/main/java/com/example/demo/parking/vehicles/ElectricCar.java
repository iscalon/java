package com.example.demo.parking.vehicles;

import com.example.demo.parking.Dimensions;

public class ElectricCar extends ElectricVehicle {

    public ElectricCar(Dimensions dimensions) {
        super(dimensions);
    }

    @Override
    public String getType() {
        return "electric-car";
    }
}
