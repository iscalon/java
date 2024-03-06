package com.nico.driving.core;

public interface SteeringPort {
    void turn(Direction direction, double angleInDegree, long timeInMillisecond);
}
