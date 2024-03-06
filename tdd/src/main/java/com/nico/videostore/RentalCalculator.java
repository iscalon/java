package com.nico.videostore;

import java.util.ArrayList;
import java.util.Collection;

public class RentalCalculator {

  private final Collection<Rental> rentals = new ArrayList<>();

  public void addRental(String title, int days) {
    rentals.add(new Rental(title, days));
  }

  public double getRentalFee() {
    return rentals.stream()
            .mapToDouble(Rental::getFee)
            .sum();
  }

  public int getRenterPoints() {
    return rentals.stream()
            .mapToInt(Rental::getPoints)
            .sum();
  }
}
