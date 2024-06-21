package com.example.demo;

import com.example.demo.parking.Dimensions;
import com.example.demo.parking.Parking;
import com.example.demo.parking.Place;
import com.example.demo.parking.vehicles.Car;
import com.example.demo.parking.vehicles.ElectricCar;
import com.example.demo.parking.vehicles.Moto;
import com.example.demo.parking.vehicles.Vehicle;
import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.sampling.ListSampler;
import org.apache.commons.rng.simple.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ParkingSimulator implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParkingSimulator.class);

    private static final RestorableUniformRandomProvider RANDOM_PROVIDER = RandomSource.XO_RO_SHI_RO_128_PP.create();

    private static final int PLACES_COUNT = 10;
    private static final int VEHICLES_COUNT = 15;
    private static final Dimensions PLACE_MAX_DIMENSIONS = new Dimensions(500, 500, 500);
    private static final Dimensions VEHICLE_MAX_DIMENSIONS = new Dimensions(PLACE_MAX_DIMENSIONS.length() - 50, PLACE_MAX_DIMENSIONS.width() - 50, PLACE_MAX_DIMENSIONS.height() - 50);
    private static final Dimensions PLACE_MIN_DIMENSIONS = new Dimensions(VEHICLE_MAX_DIMENSIONS.length(), VEHICLE_MAX_DIMENSIONS.width(), VEHICLE_MAX_DIMENSIONS.height());
    private static final Dimensions VEHICLE_MIN_DIMENSIONS = new Dimensions(100, 100, 80);

    /**
     * La classe Parking est "immutable" donc "thread-safe", par contre les références sur un parking ne sont pas "thread-safe".
     * Il conviendra donc de synchroniser les accès à la référence lors des utilisations de celle-ci.
     */
    private static final AtomicReference<Parking> PARKING_REFERENCE = new AtomicReference<>(createParking());

    private static final Deque<Vehicle> VEHICLES = createVehicles();

    @Override
    public void run(ApplicationArguments args) {
        while(!VEHICLES.isEmpty()) {
            final Vehicle vehicleGate1 = VEHICLES.pop();
            new Thread(() -> performEntry(vehicleGate1), "Gate-1").start();
            if(!VEHICLES.isEmpty()) {
                final Vehicle vehicleGate2 = VEHICLES.pop();
                new Thread(() -> performEntry(vehicleGate2), "Gate-2").start();
            }
            if(!VEHICLES.isEmpty()) {
                final Vehicle vehicleGate3 = VEHICLES.pop();
                new Thread(() -> performEntry(vehicleGate3), "Gate-3").start();
            }
        }
    }

    private static void performEntry(Vehicle vehicle) {
        synchronized (PARKING_REFERENCE) {
            PARKING_REFERENCE.updateAndGet(parking -> parking.performEntry(vehicle));
        }
    }

    private static Parking createParking() {
        return new Parking(createRandomPlaces());
    }

    private static Collection<Place> createRandomPlaces() {
        List<Place> places = new ArrayList<>(PLACES_COUNT);
        for(int i = 0 ; i < PLACES_COUNT ; i++) {
            Place place = createRandomPlace();
            places.add(place);
            LOGGER.info("{} added to parking", place);
        }
        return List.copyOf(places);
    }

    private static Place createRandomPlace() {
        return new Place(true, createRandomPlaceDimensions(), createRandomAcceptedVehicleTypes());
    }

    private static Set<String> createRandomAcceptedVehicleTypes() {
        List<String> types = List.of("thermic-car", "electric-car", "thermic-moto");
        int typesCount = RANDOM_PROVIDER.nextInt(types.size());
        if(typesCount == 0) {
            return Set.of();
        }
        return Set.copyOf(ListSampler.sample(RANDOM_PROVIDER, types, typesCount));
    }

    private static Dimensions createRandomPlaceDimensions() {
        double length = RANDOM_PROVIDER.nextDouble(PLACE_MIN_DIMENSIONS.length(), PLACE_MAX_DIMENSIONS.length());
        double width = RANDOM_PROVIDER.nextDouble(PLACE_MIN_DIMENSIONS.width(), PLACE_MAX_DIMENSIONS.width());
        double height = RANDOM_PROVIDER.nextDouble(PLACE_MIN_DIMENSIONS.height(), PLACE_MAX_DIMENSIONS.height());
        return new Dimensions(length, width, height);
    }

    private static Deque<Vehicle> createVehicles() {
        return IntStream.rangeClosed(1, VEHICLES_COUNT)
                .mapToObj(value -> createRandomVehicle())
                .collect(Collectors.toCollection(ArrayDeque::new));
    }

    private static Dimensions createRandomVehicleDimensions() {
        double length = RANDOM_PROVIDER.nextDouble(VEHICLE_MIN_DIMENSIONS.length(), VEHICLE_MAX_DIMENSIONS.length());
        double width = RANDOM_PROVIDER.nextDouble(VEHICLE_MIN_DIMENSIONS.width(), VEHICLE_MAX_DIMENSIONS.width());
        double height = RANDOM_PROVIDER.nextDouble(VEHICLE_MIN_DIMENSIONS.height(), VEHICLE_MAX_DIMENSIONS.height());
        return new Dimensions(length, width, height);
    }

    private static Vehicle createRandomVehicle() {
        String type = createRandomAcceptedVehicleTypes().stream().findAny().orElse("");
        Dimensions dimensions = createRandomVehicleDimensions();
        return switch (type) {
            case "thermic-moto" -> new Moto(dimensions);
            case "electric-car" -> new ElectricCar(dimensions);
            default -> new Car(dimensions);
        };
    }
}
