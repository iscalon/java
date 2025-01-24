package fr.nico.neural.network.one.shared;

import static org.assertj.core.api.Assertions.assertThat;

import fr.nico.neural.network.one.application.shared.DataNormalizer;
import java.util.List;
import org.junit.jupiter.api.Test;

class DataNormalizerTest {

  @Test
  void normalizeToInterval() {
    DataNormalizer dataNormalizer = DataNormalizer.toInterval(-1, 1).fromInterval(0, 5);
    List<Double> normalizedValues =
        dataNormalizer.normalize(0.15, 0.25, 0.5, 0.75, 1, 1.25, 1.5, 1.75, 2);

    assertThat(normalizedValues)
        .containsExactly(
            -0.94, -0.9, -0.8, -0.7, -0.6, -0.5, -0.4, -0.30000000000000004, -0.19999999999999996);
  }

  @Test
  void denormalizeToInterval() {
    DataNormalizer dataNormalizer = DataNormalizer.toInterval(0, 5).fromInterval(-1, 1);
    List<Double> normalizedValues =
        dataNormalizer.normalize(
            -0.94, -0.9, -0.8, -0.7, -0.6, -0.5, -0.4, -0.30000000000000004, -0.19999999999999996);

    assertThat(normalizedValues)
        .containsExactly(
            0.15000000000000013,
            0.24999999999999994,
            0.4999999999999999,
            0.7500000000000001,
            1.0,
            1.25,
            1.5,
            1.75,
            2.0);
  }

  @Test
  void normalize() {
    DataNormalizer dataNormalizer = DataNormalizer.toInterval(-1, 1);
    List<Double> normalizedValues =
        dataNormalizer.normalize(0.15, 0.25, 0.5, 0.75, 1, 1.25, 1.5, 1.75, 2);

    assertThat(normalizedValues)
        .containsExactly(
            -1.0,
            -0.8918918918918919,
            -0.6216216216216217,
            -0.3513513513513514,
            -0.08108108108108114,
            0.18918918918918926,
            0.45945945945945943,
            0.7297297297297298,
            1.0);
  }
}
