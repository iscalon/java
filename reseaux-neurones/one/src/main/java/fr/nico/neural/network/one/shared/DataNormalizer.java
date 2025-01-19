package fr.nico.neural.network.one.shared;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;

public class DataNormalizer {

  private final Pair<Double, Double> targetInterval;
  private final Pair<Double, Double> sourceInterval;

  private DataNormalizer(double intervalMinValue, double intervalMaxValue) {
    this(intervalMinValue, intervalMaxValue, Double.NaN, Double.NaN);
  }

  private DataNormalizer(
      double intervalMinValue,
      double intervalMaxValue,
      double sourceIntervalMinValue,
      double sourceIntervalMaxValue) {
    this.targetInterval = Pair.of(intervalMinValue, intervalMaxValue);
    this.sourceInterval = Pair.of(sourceIntervalMinValue, sourceIntervalMaxValue);
  }

  public static DataNormalizer toInterval(double minValue, double maxValue) {
    Interval interval = createInterval(minValue, maxValue);
    return new DataNormalizer(interval.minValue(), interval.maxValue());
  }

  public DataNormalizer fromInterval(double minValue, double maxValue) {
    if (Double.compare(minValue, maxValue) == 0) {
      throw new IllegalArgumentException(
          "Min " + minValue + " et Max " + maxValue + " ne peuvent être égaux");
    }
    if (Double.compare(minValue, maxValue) > 0) {
      double tmp = maxValue;
      maxValue = minValue;
      minValue = tmp;
    }
    return new DataNormalizer(
        this.targetInterval.getLeft(), this.targetInterval.getRight(), minValue, maxValue);
  }

  public List<Double> normalize(List<? extends Number> values) {
    double sourceIntervalLeft = sourceInterval.getLeft();
    double sourceIntervalRight = sourceInterval.getRight();
    if (Double.isNaN(sourceIntervalLeft) || Double.isNaN(sourceIntervalRight)) {
      return normalizeFromInterval(values, computeMin(values), computeMax(values));
    }
    return normalizeFromInterval(values, sourceIntervalLeft, sourceIntervalRight);
  }

  public List<Double> normalize(Number... values) {
    return normalize(Arrays.asList(values));
  }

  private List<Double> normalizeFromInterval(
      List<? extends Number> values, double minValue, double maxValue) {
    if (Double.compare(minValue, maxValue) == 0) {
      throw new IllegalArgumentException("Les valeurs semblent être constantes");
    }

    Pair<Double, Double> valuesInterval = Pair.of(minValue, maxValue);
    return Optional.ofNullable(values).orElseGet(List::of).stream()
        .map(value -> normalize(value, valuesInterval))
        .toList();
  }

  private static Interval createInterval(double minValue, double maxValue) {
    if (Double.compare(minValue, maxValue) == 0) {
      throw new IllegalArgumentException(
          "Min " + minValue + " et Max " + maxValue + " ne peuvent être égaux");
    }
    if (Double.compare(minValue, maxValue) > 0) {
      double tmp = maxValue;
      maxValue = minValue;
      minValue = tmp;
    }
    return new Interval(minValue, maxValue);
  }

  private double normalize(Number value, Pair<Double, Double> valuesInterval) {
    return (((value.doubleValue() - valuesInterval.getLeft())
                * (targetInterval.getRight() - targetInterval.getLeft()))
            / (valuesInterval.getRight() - valuesInterval.getLeft()))
        + targetInterval.getLeft();
  }

  private double computeMin(List<? extends Number> values) {
    return values.stream()
        .filter(Objects::nonNull)
        .mapToDouble(Number::doubleValue)
        .min()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Pas de minimum trouvé sur l'ensemble des valeurs passées"));
  }

  private double computeMax(List<? extends Number> values) {
    return values.stream()
        .filter(Objects::nonNull)
        .mapToDouble(Number::doubleValue)
        .max()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Pas de maximum trouvé sur l'ensemble des valeurs passées"));
  }

  private record Interval(double minValue, double maxValue) {}
}
