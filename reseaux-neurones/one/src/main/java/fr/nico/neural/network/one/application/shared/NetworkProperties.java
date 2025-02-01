package fr.nico.neural.network.one.application.shared;

import lombok.Builder;

import static java.util.Objects.requireNonNull;

@Builder
public record NetworkProperties(
    String filePath, Interval xInterval, Interval yInterval, Interval normalizedInterval) {

  public NetworkProperties {
    requireNonNull(xInterval);
    requireNonNull(yInterval);
    if (normalizedInterval == null) {
      normalizedInterval = new Interval(-1, 1);
    }
  }
}
