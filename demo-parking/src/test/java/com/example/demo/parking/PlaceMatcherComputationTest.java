package com.example.demo.parking;

import com.example.demo.parking.entryhandlers.VehicleEntryPlaceMatcher;
import com.example.demo.parking.vehicles.Car;
import com.example.demo.parking.vehicles.Moto;
import com.example.demo.parking.vehicles.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceMatcherComputationTest {

    public static final String[] UNRESTRICTED_VEHICLE_TYPES = {};

    @Test
    @DisplayName("Ranking computation returns rank = 0 for non free place with matching dimensions")
    void test01() {
        Dimensions dimensions = new Dimensions(200, 150, 150);
        VehicleEntryPlaceMatcher matcher = createVehicleMatcherForUnfreePlaceWithDimensions(dimensions);
        double rank = matcher.computeRankFor(createThermicCar(dimensions));
        assertThat(rank).isZero();
    }

    @Test
    @DisplayName("Ranking computation returns rank = 0 for non free place with unmatching dimensions")
    void test02() {
        Dimensions placeDimensions = new Dimensions(200, 150, 150);
        VehicleEntryPlaceMatcher matcher = createVehicleMatcherForUnfreePlaceWithDimensions(placeDimensions);
        Dimensions placeOversizedDimensions = placeDimensions.withLength(placeDimensions.length() + 100);
        double rank = matcher.computeRankFor(createThermicCar(placeOversizedDimensions));
        assertThat(rank).isZero();
    }

    @Test
    @DisplayName("Ranking computation returns rank = 0 for free place with unmatching dimensions")
    void test03() {
        Dimensions placeDimensions = new Dimensions(200, 150, 150);
        VehicleEntryPlaceMatcher matcher = createUnrestrictedVehicleTypeMatcherForFreePlaceWithDimensions(placeDimensions);
        Dimensions placeOversizedDimensions = placeDimensions.withLength(placeDimensions.length() + 100);
        double rank = matcher.computeRankFor(createThermicCar(placeOversizedDimensions));
        assertThat(rank).isZero();
    }

    @Test
    @DisplayName("Ranking computation returns rank >= 1 for free place with matching dimensions")
    void test04() {
        Dimensions dimensions = new Dimensions(200, 150, 150);
        VehicleEntryPlaceMatcher matcher = createUnrestrictedVehicleTypeMatcherForFreePlaceWithDimensions(dimensions);
        double rank = matcher.computeRankFor(createThermicCar(dimensions));
        assertThat(rank).isGreaterThanOrEqualTo(1d);
    }

    @Test
    @DisplayName("Ranking computation returns rank = 0 for free place that has only unmatching vehicle type")
    void test05() {
        Dimensions dimensions = new Dimensions(200, 150, 150);
        VehicleEntryPlaceMatcher matcher = createVehicleMatcherForFreePlaceWithDimensions(dimensions, "moto");
        double rank = matcher.computeRankFor(createThermicCar(dimensions));
        assertThat(rank).isZero();
    }

    @Test
    @DisplayName("Ranking computation returns rank >= 1 for free place that has matching vehicle type")
    void test06() {
        Dimensions dimensions = new Dimensions(200, 150, 150);
        VehicleEntryPlaceMatcher matcher = createVehicleMatcherForFreePlaceWithDimensions(dimensions, "thermic-car");
        double rank = matcher.computeRankFor(createThermicCar(dimensions));
        assertThat(rank).isGreaterThanOrEqualTo(1d);
    }

    @Test
    @DisplayName("""
            GIVEN two thermic cars with different dimensions
            WHEN computing rank for a free place that matches all criteria but with dimensions that are closer to the second car's dimensions
            THEN computed rank for first car is lower than the second car's rank
            """)
    void test07() {
        // Arrange
        Dimensions placeDimensions = new Dimensions(120, 80, 100);
        Dimensions firstCarDimensions = new Dimensions(placeDimensions.length() - 10, placeDimensions.width() - 20, placeDimensions.height() - 5);
        Vehicle car1 = createThermicCar(firstCarDimensions);
        Dimensions secondCarDimensions = new Dimensions(placeDimensions.length() - 1, placeDimensions.width() - 1, placeDimensions.height() - 5);
        Vehicle car2 = createThermicCar(secondCarDimensions);
        VehicleEntryPlaceMatcher matcher = new VehicleEntryPlaceMatcher(new Place(placeDimensions));

        // Act
        double car1Rank = matcher.computeRankFor(car1);
        double car2Rank = matcher.computeRankFor(car2);

        // Assert
        assertThat(car1Rank).isLessThan(car2Rank);
    }

    @Test
    @DisplayName("""
            GIVEN two vehicles : one car and one moto
            WHEN computing rank for a free place that accept all vehicle types
            THEN computed rank for the moto is lower than the car's rank
            """)
    void test08() {
        // Arrange
        Dimensions placeDimensions = new Dimensions(120, 80, 100);
        Dimensions carDimensions = new Dimensions(placeDimensions.length() - 10, placeDimensions.width() - 20, placeDimensions.height() - 5);
        Vehicle car = createThermicCar(carDimensions);
        Dimensions motoDimensions = new Dimensions(placeDimensions.length() - 80, placeDimensions.width() - 45, placeDimensions.height() - 30);
        Vehicle moto = new Moto(motoDimensions);
        VehicleEntryPlaceMatcher matcher = new VehicleEntryPlaceMatcher(new Place(placeDimensions));

        // Act
        double carRank = matcher.computeRankFor(car);
        double motoRank = matcher.computeRankFor(moto);

        // Assert
        assertThat(motoRank).isLessThan(carRank);
    }


    @Test
    @DisplayName("""
            GIVEN two vehicles : one car and one moto
            WHEN computing rank for a free place that accept only moto types
            THEN computed rank for the car is lower than the moto's rank
            """)
    void test09() {
        // Arrange
        Dimensions placeDimensions = new Dimensions(120, 80, 100);
        Dimensions carDimensions = new Dimensions(placeDimensions.length() - 10, placeDimensions.width() - 20, placeDimensions.height() - 5);
        Vehicle car = createThermicCar(carDimensions);
        Dimensions motoDimensions = new Dimensions(placeDimensions.length() - 80, placeDimensions.width() - 45, placeDimensions.height() - 30);
        Vehicle moto = new Moto(motoDimensions);
        VehicleEntryPlaceMatcher matcher = createVehicleMatcherForFreePlaceWithDimensions(placeDimensions, "thermic-moto", "electric-moto");

        // Act
        double carRank = matcher.computeRankFor(car);
        double motoRank = matcher.computeRankFor(moto);

        // Assert
        assertThat(carRank).isLessThan(motoRank);
    }

    private static VehicleEntryPlaceMatcher createVehicleMatcherForUnfreePlaceWithDimensions(Dimensions placeDimensions) {
        return new VehicleEntryPlaceMatcher(new Place(false, placeDimensions));
    }

    private static VehicleEntryPlaceMatcher createUnrestrictedVehicleTypeMatcherForFreePlaceWithDimensions(Dimensions placeDimensions) {
        return createVehicleMatcherForFreePlaceWithDimensions(placeDimensions, UNRESTRICTED_VEHICLE_TYPES);
    }

    private static VehicleEntryPlaceMatcher createVehicleMatcherForFreePlaceWithDimensions(Dimensions placeDimensions, String... acceptedVehicleTypes) {
        Place place = new Place(true, placeDimensions);
        return new VehicleEntryPlaceMatcher(place.addAcceptedVehicleTypes(acceptedVehicleTypes));
    }

    private static Car createThermicCar(Dimensions dimensions) {
        return new Car(dimensions);
    }
}
