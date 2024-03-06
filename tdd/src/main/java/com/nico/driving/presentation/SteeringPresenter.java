package com.nico.driving.presentation;

import com.nico.driving.core.Direction;
import com.nico.driving.core.SteeringPort;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class SteeringPresenter implements SteeringPort {
  private final SteeringWheelService steeringWheelService;

  public SteeringPresenter(SteeringWheelService steeringWheelService) {
    this.steeringWheelService = requireNonNull(steeringWheelService);
  }

  @Override
  public void turn(Direction direction, double angleInDegree, long timeInMillisecond) {
    Command command = new Command(10, direction == Direction.RIGHT, (int) timeInMillisecond / 10, 0);
    steeringWheelService.perform(List.of(command));
  }
}
