package com.example.demo.parking.vehicles;

import com.example.demo.parking.Dimensions;

public class Moto extends ThermicVehicle {

    public Moto(Dimensions dimensions) {
        super(dimensions);
    }

    @Override
    public String getType() {
        return "thermic-moto";
    }
}
