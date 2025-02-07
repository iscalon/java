package fr.nico.neural.network.three.application;

import static fr.nico.neural.network.one.data.normalize.CsvXYFileNormalizer.getFile;

import fr.nico.neural.network.one.application.shared.*;
import fr.nico.neural.network.one.graphic.XYSampleChart;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.math.NumberUtils;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeuralNetworkThreeTesting {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeuralNetworkThreeTesting.class);

  private NeuralNetworkThreeTesting() {
    // just to hide it
  }

  public static Function<List<? extends Number> /* input */, Number /* output */>
      getComputingFunction(NetworkProperties properties) {
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
      double normalizedOutput = network.compute(new BasicMLData(normalizedInput)).getData(0);
      // On remet le y calculé sur l'intervalle non normalisé
      return yProjection.normalize(normalizedOutput).getFirst();
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
            .filePath("Prix_Maisons_2011-2024_Windowed_4Trimestres.network")
            .xInterval(new Interval(-100, 100))
            .yInterval(new Interval(-100, 100))
            .build();
    LOGGER.info("Test du réseau : {}", properties.filePath());

    Function<List<? extends Number>, Number> computingFunction = getComputingFunction(properties);

    Scanner scanner = new Scanner(System.in);
    LOGGER.info("Combien de trimestres à estimer ? : ");
    int trimestreCount = scanner.nextInt();
    LOGGER.info(
        "Donnez les valeurs des variations de prix des 4 derniers trimestres connus (1 par 1 suivi de la touche 'ENTER'): ");
    List<Double> diff4DerniersTrimestres = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      String next = scanner.next();
      double value = NumberUtils.toDouble(next);
      diff4DerniersTrimestres.add(value);
    }

    List<Double> resultat = new ArrayList<>();
    List<Double> mois = Stream.iterate(0d, i -> i + 1).limit(trimestreCount).toList();
    for (int i = 0; i < trimestreCount; i++) {
      double differencePrevue = computingFunction.apply(diff4DerniersTrimestres).doubleValue();
      diff4DerniersTrimestres =
          new ArrayList<>(diff4DerniersTrimestres.subList(1, diff4DerniersTrimestres.size()));
      diff4DerniersTrimestres.add(differencePrevue);
      resultat.add(differencePrevue);
    }

    drawAndSave(mois, resultat);
  }

  private static void drawAndSave(List<Double> x, List<Double> y) {
    LOGGER.info("X={}", x);
    LOGGER.info("Y={}", y);
    XYSampleChart chart =
        new XYSampleChart(
            XYGraphProperties.builder()
                .graphicName("Prévisions")
                .width(1600)
                .height(1200)
                .xAxisTitle("Trimestre")
                .yAxisTitle("Différence %")
                .build());

    chart.addXYSerie(
        "Evolution", x, y, new XYSerieProperties(XChartSeriesColors.PURPLE, SeriesLines.SOLID));

    chart.export();
    chart.display();
  }
}
