package com.example.demo.parking;

public record Dimensions(double length, double width, double height) {

    public boolean contains(Dimensions otherDimensions) {
        return length >= otherDimensions.length && width >= otherDimensions.width && height >= otherDimensions.height;
    }

    public Dimensions withLength(double length) {
        return new Dimensions(length, this.width(), this.height());
    }
}
