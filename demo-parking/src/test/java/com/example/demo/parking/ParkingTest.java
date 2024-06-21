package com.example.demo.parking;

import com.example.demo.parking.vehicles.Car;
import com.example.demo.parking.vehicles.ElectricCar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ParkingTest {

    public static final int MAX_SIZE = 10000;
    public static final Dimensions MAX_DIMENSIONS = new Dimensions(MAX_SIZE, MAX_SIZE, MAX_SIZE);

    @Test
    @DisplayName("""
            GIVEN a parking with one free place matching all vehicle type and dimensions
            WHEN a vehicle tries to enter in the parking
            THEN its entry is correctly performed
            """)
    void test01() {
        Parking parking = new Parking(List.of(createFreeNoVehicleRestrictionPlace()));
        Car car = createCar();

        parking = parking.performEntry(car);

        Optional<Place> place = car.getPlace();
        assertThat(place)
                .map(Place::isFree)
                .hasValue(false);
        assertThat(parking.getPlaces())
                .contains(place.orElseThrow());
    }

    @Test
    @DisplayName("""
            GIVEN a parking with no place
            WHEN a vehicle tries to enter in the parking
            THEN its entry cannot be performed
            """)
    void test02() {
        Parking parking = new Parking(List.of());
        Car car = createCar();

        parking.performEntry(car);

        Optional<Place> place = car.getPlace();
        assertThat(place)
                .isEmpty();
    }

    @Test
    @DisplayName("""
            GIVEN a parking with one free place that doesn't match 'thermic-car' vehicle type
            WHEN a vehicle that is a 'thermic-car' tries to enter in the parking
            THEN its entry cannot be performed
            """)
    void test03() {
        Parking parking = new Parking(List.of(new Place(true, MAX_DIMENSIONS, Set.of("electric-car"))));
        Car car = createCar();

        parking.performEntry(car);

        Optional<Place> place = car.getPlace();
        assertThat(place)
                .isEmpty();
    }

    @Test
    @DisplayName("""
            GIVEN a parking with two free places that match every vehicle type, but the 2nd place dimensions are better fitted to the entering vehicle
            WHEN a vehicle tries to enter in the parking
            THEN its entry can be performed on the 2nd place
            """)
    void test04() {
        Place firstPlace = createFreeNoVehicleRestrictionPlace();
        Place secondPlace = new Place(new Dimensions(210, 110, 110));

        Parking parking = new Parking(List.of(firstPlace, secondPlace));
        Car car = createCar();

        parking.performEntry(car);

        Optional<Place> place = car.getPlace();
        assertThat(place)
                .map(Place::getId)
                .hasValue(secondPlace.getId());
    }

    @Test
    @DisplayName("""
            GIVEN a parking with two free places which can accept every vehicle dimension but the 1st one is for electric car only and the other is for every vehicle type
            WHEN an electric car tries to enter in the parking
            THEN its entry can be performed on the first place
            """)
    void test05() {
        Dimensions placeDimensions = MAX_DIMENSIONS;
        Place firstPlace = new Place(true, placeDimensions, Set.of("electric-car"));
        Place secondPlace = new Place(placeDimensions);

        Parking parking = new Parking(List.of(firstPlace, secondPlace));
        ElectricCar car = createElectricCar();

        parking.performEntry(car);

        assertThat(car.getPlace())
                .map(Place::getId)
                .hasValue(firstPlace.getId());
    }

    @Test
    @DisplayName("""
            GIVEN a parking with two free places which can accept every vehicle dimension but the 1st one is for electric car only and the other one is for both electric and thermic car
            WHEN an electric car tries to enter in the parking
            THEN its entry can be performed on the second place so the thermic car place can stay available to a thermic car
            """)
    void test06() {
        Dimensions placeDimensions = MAX_DIMENSIONS;
        Place firstPlace = new Place(true, placeDimensions, Set.of("electric-car", "thermic-car"));
        Place secondPlace = new Place(true, placeDimensions, Set.of("electric-car"));

        Parking parking = new Parking(List.of(firstPlace, secondPlace));
        ElectricCar car = createElectricCar();

        parking.performEntry(car);

        assertThat(car.getPlace())
                .map(Place::getId)
                .hasValue(secondPlace.getId());
    }

    @Test
    @DisplayName("""
            GIVEN a parking with one free place matching all vehicle type and dimensions
            WHEN two vehicles try to enter in the parking
            THEN first vehicle entry is correctly performed but the second cannot be done
            """)
    void test07() {
        Parking parking = new Parking(List.of(createFreeNoVehicleRestrictionPlace()));
        Car car1 = createCar();
        Car car2 = createCar();

        parking = parking.performEntry(car1);
        parking.performEntry(car2);

        Optional<Place> car1Place = car1.getPlace();
        Optional<Place> car2Place = car2.getPlace();

        assertThat(car1Place).isNotEmpty();
        assertThat(car2Place).isEmpty();
    }

    @Test
    @DisplayName("A vehicle exit on a full parking allows a new vehicle entry")
    void test08() {
        Parking parking = new Parking(List.of(createFreeNoVehicleRestrictionPlace()));
        Car car1 = createCar();
        Car car2 = createCar();

        parking = parking.performEntry(car1);
        parking = parking.performEntry(car2);

        Optional<Place> car1Place = car1.getPlace();
        Optional<Place> car2Place = car2.getPlace();
        assertThat(car1Place).isNotEmpty();
        assertThat(car2Place).isEmpty();

        parking = parking.performExit(car1);
        parking.performEntry(car2);

        car1Place = car1.getPlace();
        car2Place = car2.getPlace();
        assertThat(car1Place).isEmpty();
        assertThat(car2Place).isNotEmpty();
    }

    private static Car createCar() {
        return new Car(new Dimensions(200, 100, 100));
    }

    private static ElectricCar createElectricCar() {
        return new ElectricCar(new Dimensions(190, 100, 90));
    }

    private static Place createFreeNoVehicleRestrictionPlace() {
        return new Place(MAX_DIMENSIONS);
    }
}
