package com.example.demo.parking;

import com.example.demo.parking.entryhandlers.VehicleEntryPlaceMatcher;
import com.example.demo.parking.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class Parking {

    private static final Logger log = LoggerFactory.getLogger(Parking.class);

    private final Set<Place> places;

    public Parking(Collection<Place> places) {
        this.places = Set.copyOf(places);
    }

    private static boolean isMatching(Place place, Vehicle vehicle) {
        return !isZero(new VehicleEntryPlaceMatcher(place)
                .computeRankFor(vehicle));
    }

    private static boolean isZero(double value) {
        return Double.compare(value, 0) == 0;
    }

    public Parking performEntry(Vehicle vehicle) {
        log.info("{} tries to enter parking {}", vehicle, this);
        Optional<Place> placeToLock = findBestPlace(vehicle);
        return placeToLock
                .map(candidate -> new Parking(registerPlaceWith(candidate, vehicle)))
                .orElse(this);
    }

    public Parking performExit(Vehicle vehicle) {
        log.info("{} is leaving the parking", vehicle);
        Optional<Place> placeToFree = findPlaceToFree(vehicle);

        return placeToFree
                .map(candidate -> new Parking(unregisterPlaceWith(candidate, vehicle)))
                .orElse(this);
    }

    private Optional<Place> findPlaceToFree(Vehicle vehicle) {
        return vehicle.getPlace()
                .map(Place::getId)
                .flatMap(id -> places.stream()
                        .filter(candidate -> Objects.equals(id, candidate.getId()))
                        .findAny());
    }

    private Set<Place> unregisterPlaceWith(Place place, Vehicle vehicle) {
        Place freedPlace = place.setFree(true);
        vehicle.setPlace(null);
        log.info("{} left {}", vehicle, freedPlace);
        return updatePlaces(freedPlace);
    }

    private Set<Place> registerPlaceWith(Place place, Vehicle vehicle) {
        Place givenPlace = place.setFree(false);
        vehicle.setPlace(givenPlace);
        log.info("In parking {}, {} entered at {}", this, vehicle, givenPlace);
        return updatePlaces(givenPlace);
    }

    private Set<Place> updatePlaces(Place place) {
        Set<Place> updatedPlaces = this.places.stream()
                .filter(candidate -> !Objects.equals(candidate.getId(), place.getId()))
                .collect(Collectors.toCollection(HashSet::new));
        updatedPlaces.add(place);
        return updatedPlaces;
    }

    private Optional<Place> findBestPlace(Vehicle vehicle) {
        return places.stream()
                .filter(place -> isMatching(place, vehicle))
                .max((place1, place2) -> comparePlacesForVehicle(place1, place2, vehicle));
    }

    private int comparePlacesForVehicle(Place place1, Place place2, Vehicle vehicle) {
        double rank1 = new VehicleEntryPlaceMatcher(place1)
                .computeRankFor(vehicle);

        double rank2 = new VehicleEntryPlaceMatcher(place2)
                .computeRankFor(vehicle);

        return Double.compare(rank1, rank2);
    }

    public Set<Place> getPlaces() {
        return places;
    }
}
