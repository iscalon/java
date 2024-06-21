package com.example.demo.parking;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Place {

    private final String id;

    private final Dimensions dimensions;

    private final boolean free;

    private final Set<String> acceptedVehicleTypes;

    public Place(Dimensions dimensions) {
        this(true, dimensions);
    }

    public Place(boolean free, Dimensions dimensions) {
       this(free, dimensions, new HashSet<>());
    }

    public Place(boolean free, Dimensions dimensions, Set<String> acceptedVehicleTypes) {
        this(UUID.randomUUID().toString(), free, dimensions, acceptedVehicleTypes);
    }

    private Place(String id, boolean free, Dimensions dimensions, Set<String> acceptedVehicleTypes) {
        this.id = id;
        this.dimensions = dimensions;
        this.free = free;
        this.acceptedVehicleTypes = acceptedVehicleTypes;
    }

    public boolean isFree() {
        return free;
    }

    public Place setFree(boolean free) {
        return new Place(getId(), free, this.getDimensions(), Set.copyOf(this.acceptedVehicleTypes));
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public Place addAcceptedVehicleTypes(String... types) {
        Set<String> typesToAdd = new HashSet<>(Arrays.asList(types));
        typesToAdd.addAll(this.acceptedVehicleTypes);
        return new Place(getId(), isFree(), getDimensions(), typesToAdd);
    }

    public boolean isAcceptedVehicleType(String type) {
        if(acceptedVehicleTypes.isEmpty()) {
            return true;
        }
        return acceptedVehicleTypes.contains(type);
    }

    public int getAcceptedVehicleTypesCount() {
        return this.acceptedVehicleTypes.size();
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Place{" +
                "id='" + id + '\'' +
                ", dimensions=" + dimensions +
                ", free=" + free +
                ", acceptedVehicleTypes=" + acceptedVehicleTypes +
                '}';
    }
}
