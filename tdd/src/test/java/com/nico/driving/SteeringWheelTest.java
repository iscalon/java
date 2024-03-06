package com.nico.driving;

import com.nico.driving.core.Direction;
import com.nico.driving.core.SteeringPort;
import com.nico.driving.presentation.Command;
import com.nico.driving.presentation.SteeringPresenter;
import com.nico.driving.presentation.SteeringWheelService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SteeringWheelTest {

    @Test
    @DisplayName("driving AI should correctly move the steering wheel controller")
    void test01() {
        SteeringWheelService steeringWheelServiceSpy = spy(SteeringWheelService.class);
        SteeringPort steeringPort = new SteeringPresenter(steeringWheelServiceSpy);

        double angleInDegree = 30;
        long timeInMillisecond = 2300;
        steeringPort.turn(Direction.RIGHT, angleInDegree, timeInMillisecond);

        verify(steeringWheelServiceSpy)
                .perform(expectedCommands());
    }

    private static List<Command> expectedCommands() {
        int steps = 10;
        boolean direction = true;
        int stepTime = 230;
        int delay = 0;
        return List.of(
                new Command(steps, direction, stepTime, delay));
    }
}
