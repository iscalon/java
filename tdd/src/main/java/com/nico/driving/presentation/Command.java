package com.nico.driving.presentation;

public record Command(int steps, boolean right, int stepTime, int delay) {}
