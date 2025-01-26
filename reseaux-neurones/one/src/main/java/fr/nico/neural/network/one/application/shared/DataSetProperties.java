package fr.nico.neural.network.one.application.shared;

import static java.util.Objects.requireNonNull;

import lombok.Builder;

@Builder
public record DataSetProperties(
    String fileName,
    int numberOfInput,
    int numberOfOutput,
    boolean considerHeader,
    boolean significance,
    Interval xInterval,
    Interval yInterval,
    Interval normalizedInterval) {

  public DataSetProperties {
    requireNonNull(xInterval);
    requireNonNull(yInterval);
    if (normalizedInterval == null) {
      normalizedInterval = new Interval(-1, 1);
    }
  }
}
