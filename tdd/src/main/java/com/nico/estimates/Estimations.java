package com.nico.estimates;

import static com.nico.estimates.Estimation.ROUNDING_MODE;
import static com.nico.estimates.Estimation.SCALE;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Estimations {

  private final List<Estimation> estimates;

  private Estimations(Estimation... estimates) {
    this.estimates = new ArrayList<>(Stream.of(estimates).toList());
  }

  public static Estimations of(Estimation... estimations) {
    return new Estimations(estimations);
  }

  private static BigDecimal add(BigDecimal n1, BigDecimal n2) {
    return n1.add(n2);
  }

  private static BigDecimal round(BigDecimal number) {
    return number.setScale(SCALE, ROUNDING_MODE);
  }

  public BigDecimal sigma() {
    if (estimates.isEmpty()) {
      return round(BigDecimal.ZERO);
    }
    BigDecimal bigSigma =
        estimates.stream()
            .map(Estimation::sigma)
            .map(s -> s.pow(2))
            .reduce(BigDecimal.ZERO, Estimations::add)
            .sqrt(new MathContext(64, ROUNDING_MODE));
    return round(bigSigma);
  }

  public BigDecimal mu() {
    if (estimates.isEmpty()) {
      return round(BigDecimal.ZERO);
    }
    BigDecimal bigMu = estimates.stream()
            .map(Estimation::mu)
            .reduce(BigDecimal.ZERO, Estimations::add);
    return round(bigMu);
  }

  public void add(Estimation estimation) {
    this.estimates.add(estimation);
  }

  @Override
  public String toString() {
    return "M=" + mu() + ", Σ=" + sigma();
  }
}
