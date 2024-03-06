package com.nico.videostore;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CustomerTest {

  private RentalCalculator rentalCalculator;

  @BeforeAll
  public static void loadRegistry() {
    VideoRegistry.addMovie("Batman", VideoType.REGULAR);
    VideoRegistry.addMovie("Bambi", VideoType.CHILDREN);
  }

  @BeforeEach
  void init() {
    this.rentalCalculator = new RentalCalculator();
  }

  @Test
  @DisplayName("when regular movie is rent for one day then you earn 1 point and have to pay 1€50")
  void test01() {
    rentalCalculator.addRental("Batman", 1);

    assertFeeAndPoints(1.50d, 1);
  }

  @Test
  @DisplayName(
      "when regular movie is rent for three days then on second and third day no point is earned but it's free")
  void test02() {
    rentalCalculator.addRental("Batman", 3);

    assertFeeAndPoints(1.50d, 1);
  }

  @Test
  @DisplayName(
      "when regular movie is rent for more than three days then starting from fourth day you earn 1 point and have to pay 1€50 per day")
  void test03() {
    rentalCalculator.addRental("Batman", 4);

    assertFeeAndPoints(3.00d, 2);
  }

  @Test
  @DisplayName("when children movie is rent for one day then you earn 1 point and have to pay 1€00")
  void test04() {
    rentalCalculator.addRental("Bambi", 1);

    assertFeeAndPoints(1.00d, 1);
  }

  @Test
  @DisplayName(
      "when children movie is rent for four days then you earn 1 point and have to pay 4€00")
  void test05() {
    rentalCalculator.addRental("Bambi", 4);

    assertFeeAndPoints(4.00d, 1);
  }

  @Test
  @DisplayName(
      "when one regular movie and one childre movie are rent both for four days then you earn 3 points and have to pay 7€00")
  void test06() {
    rentalCalculator.addRental("Batman", 4);
    rentalCalculator.addRental("Bambi", 4);

    assertFeeAndPoints(7.00d, 3);
  }

  private void assertFeeAndPoints(double fee, int points) {
    assertThat(rentalCalculator.getRentalFee()).isEqualTo(fee);
    assertThat(rentalCalculator.getRenterPoints()).isEqualTo(points);
  }
}
