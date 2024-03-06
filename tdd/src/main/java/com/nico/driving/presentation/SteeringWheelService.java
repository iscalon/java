package com.nico.driving.presentation;

import java.util.List;

public interface SteeringWheelService {
    void perform(List<Command> commands);
}
