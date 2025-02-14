package fr.nico.neural.network.four.application;

import static fr.nico.neural.network.one.data.normalize.CsvXYFileNormalizer.getFile;

import fr.nico.neural.network.one.application.shared.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.UnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.apache.commons.lang3.math.NumberUtils;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeuralNetworkFourTesting {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeuralNetworkFourTesting.class);

  private NeuralNetworkFourTesting() {
    // just to hide it
  }

  public static UnaryOperator<List<? extends Number>> getComputingFunction(
      NetworkProperties properties) {
    BasicNetwork network = loadNetwork(properties.filePath());

    Interval xInterval = properties.xInterval();
    Interval yInterval = properties.yInterval();
    Interval normalizedInterval = properties.normalizedInterval();

    // Permet de normaliser les valeurs d'entrée X
    DataNormalizer xProjection =
        DataNormalizer.toInterval(normalizedInterval.minValue(), normalizedInterval.maxValue())
            .fromInterval(xInterval.minValue(), xInterval.maxValue());

    // Permet de dénormaliser les valeurs de sortie Y
    DataNormalizer yProjection =
        DataNormalizer.toInterval(yInterval.minValue(), yInterval.maxValue())
            .fromInterval(normalizedInterval.minValue(), normalizedInterval.maxValue());

    return input -> {
      double[] normalizedInput =
          xProjection.normalize(input).stream().mapToDouble(Number::doubleValue).toArray();
      double[] normalizedOutput = network.compute(new BasicMLData(normalizedInput)).getData();
      // On remet le y calculé sur l'intervalle non normalisé
      return yProjection.normalize(DoubleStream.of(normalizedOutput).boxed().toList());
    };
  }

  private static BasicNetwork loadNetwork(String networkFileName) {
    try {
      return (BasicNetwork) EncogDirectoryPersistence.loadObject(getFile(networkFileName));
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Problème lors du chargement du réseau via le fichier : " + networkFileName, e);
    }
  }

  public static void main(String[] args) {
    NetworkProperties properties =
        NetworkProperties.builder()
            .filePath("LivresEtMots.network")
            .xInterval(new Interval(1, 60))
            .yInterval(new Interval(0, 1))
            .build();
    LOGGER.info("Test du réseau : {}", properties.filePath());

    UnaryOperator<List<? extends Number>> computingFunction = getComputingFunction(properties);

    Scanner scanner = new Scanner(System.in);
    LOGGER.info(
        "Donnez les valeurs numériques des 3 mots dont on cherche à déterminer le livre (1 par 1 suivi de la touche 'ENTER'): ");
    List<Double> input = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      String next = scanner.next();
      double value = NumberUtils.toDouble(next);
      input.add(value);
    }
    List<? extends Number> livres = computingFunction.apply(input);
    LOGGER.info("Taux appartenance aux 5 livres : {}", livres);
    int indexMax =
        IntStream.range(0, livres.size())
                .reduce(
                    (i, j) -> livres.get(i).doubleValue() >= livres.get(j).doubleValue() ? i : j)
                .orElse(-1)
            + 1;

    LOGGER.info("Le triplet de mots appartient au livre n°{}", indexMax);
  }
}
