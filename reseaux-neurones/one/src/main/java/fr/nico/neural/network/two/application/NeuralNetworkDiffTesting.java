package fr.nico.neural.network.two.application;

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

public class NeuralNetworkDiffTesting {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeuralNetworkDiffTesting.class);

  private NeuralNetworkDiffTesting() {
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
            .filePath("TauxMoyens_06-1998_01-2025_Fenetre-6mois.network")
            .xInterval(new Interval(-30, 30))
            .yInterval(new Interval(-30, 30))
            .build();
    LOGGER.info("Test du réseau : {}", properties.filePath());

    Function<List<? extends Number>, Number> computingFunction = getComputingFunction(properties);

    Scanner scanner = new Scanner(System.in);
    LOGGER.info("Combien de mois à estimer ? : ");
    int monthCount = scanner.nextInt();
    LOGGER.info(
        "Donnez les valeurs des taux moyens des 7 derniers mois connus (1 par 1 suivi de la touche 'ENTER'): ");
    List<Double> tauxMoyens7DerniersMois = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      String next = scanner.next();
      double value = NumberUtils.toDouble(next);
      tauxMoyens7DerniersMois.add(value);
    }

    List<Double> differencesTauxMoyens6DerniersMois = new ArrayList<>();
    for (int i = 0; i < 6; i++) {
      differencesTauxMoyens6DerniersMois.add(
          tauxMoyens7DerniersMois.get(i + 1) - tauxMoyens7DerniersMois.get(i));
    }

    List<Double> resultat = new ArrayList<>();
    List<Double> mois = Stream.iterate(0d, i -> i + 1).limit(monthCount).toList();
    double dernierTauxConnu = tauxMoyens7DerniersMois.getLast();
    for (int i = 0; i < monthCount; i++) {
      double differencePrevue =
          computingFunction.apply(differencesTauxMoyens6DerniersMois).doubleValue();
      double tauxPrevu = dernierTauxConnu + differencePrevue;
      differencesTauxMoyens6DerniersMois =
          new ArrayList<>(differencesTauxMoyens6DerniersMois.subList(1, 6));
      differencesTauxMoyens6DerniersMois.add(differencePrevue);
      dernierTauxConnu = tauxPrevu;
      resultat.add(tauxPrevu);
    }

    drawAndSave(mois, resultat);
  }

  private static void drawAndSave(List<Double> x, List<Double> y) {
    XYSampleChart chart =
        new XYSampleChart(
            XYGraphProperties.builder()
                .graphicName("Prévisions")
                .width(800)
                .height(600)
                .xAxisTitle("Mois")
                .yAxisTitle("Taux")
                .build());

    chart.addXYSerie(
        "1€ en yens", x, y, new XYSerieProperties(XChartSeriesColors.PURPLE, SeriesLines.SOLID));

    chart.export();
    chart.display();
  }
}
