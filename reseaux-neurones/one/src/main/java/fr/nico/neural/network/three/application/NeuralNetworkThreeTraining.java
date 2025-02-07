package fr.nico.neural.network.three.application;

import static java.util.Objects.requireNonNull;

import fr.nico.neural.network.one.application.out.DataRepository;
import fr.nico.neural.network.one.application.out.XYGraphicDisplay;
import fr.nico.neural.network.one.application.out.XYGraphicHandler;
import fr.nico.neural.network.one.application.shared.DataNormalizer;
import fr.nico.neural.network.one.application.shared.DataSetProperties;
import fr.nico.neural.network.one.application.shared.XYGraphProperties;
import fr.nico.neural.network.one.application.shared.XYSerieProperties;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.knowm.xchart.style.colors.XChartSeriesColors;
import org.knowm.xchart.style.lines.SeriesLines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NeuralNetworkThreeTraining {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeuralNetworkThreeTraining.class);

  private static final ActivationFunction NO_ACTIVATION = null;
  private static final ActivationFunction DEFAULT_ACTIVATION = new ActivationTANH();
  private static final boolean HAS_BIAS = true;
  private static final boolean NO_BIAS = false;
  private static final double EXPECTED_ERROR_RATE = 0.0000003;
  private static final double DELTA = 0.00000001;
  private static final int MAXIMUM_TRAIN_LOOP_COUNT = 100000;

  private final XYGraphicHandler graphicHandlers;
  private final DataRepository<BasicNetwork> networks;

  NeuralNetworkThreeTraining(
      XYGraphicHandler graphicHandlers, DataRepository<BasicNetwork> networks) {
    this.graphicHandlers = requireNonNull(graphicHandlers);
    this.networks = requireNonNull(networks);
  }

  public void trainAndSaveNetworkUsing(DataSetProperties dataSetProperties) {
    MLDataSet dataSet = networks.readDataSet(dataSetProperties);

    LOGGER.info("Création et entrainement du réseau");
    BasicNetwork network = createNetwork(dataSet);
    trainAndSaveNetworkAndComputeGraphics(network, dataSet, dataSetProperties);
  }

  private static void exportAndDisplay(XYGraphicDisplay graphics) {
    graphics.export();
    graphics.display();
  }

  private static boolean train(BasicNetwork network, MLDataSet dataSet) {
    ResilientPropagation train = new ResilientPropagation(network, dataSet);
    int epoch = 1;
    double error = Double.NaN;
    do {
      // Lancement d'1 entrainement du réseau
      train.iteration();
      double errorBeforeTrain = train.getError();
      double errorAfterTrain = network.calculateError(dataSet);
      error = errorAfterTrain;
      LOGGER.debug(
          "Epoch #{}, erreur avant entrainement : {}, erreur après entrainement : {}",
          epoch,
          errorBeforeTrain,
          errorAfterTrain);
      epoch++;
      if (epoch >= MAXIMUM_TRAIN_LOOP_COUNT
          && Double.compare(errorAfterTrain, EXPECTED_ERROR_RATE + DELTA) > 0) {
        LOGGER.warn(
            "L'entrainement du réseau a échoué avec une erreur de {}, peut-être devriez vous relancer l'entrainement ?",
            error);
        return false;
      }

    } while (Double.compare(network.calculateError(dataSet), EXPECTED_ERROR_RATE) > 0);
    LOGGER.info("L'entrainement du réseau a réussi avec une erreur de {}", error);
    return true;
  }

  private XYGraphicDisplay initGraphics(DataSetProperties dataSetProperties) {
    return graphicHandlers.initializeGraphics(
        XYGraphProperties.builder()
            .graphicName("Training for " + dataSetProperties.fileName())
            .width(800)
            .height(600)
            .xAxisTitle("TRIMESTRE")
            .yAxisTitle("DIFF")
            .build());
  }

  private boolean trainAndSaveNetworkAndComputeGraphics(
      BasicNetwork network, MLDataSet dataSet, DataSetProperties dataSetProperties) {

    train(network, dataSet);
    String networkResults = networks.saveNetwork(dataSetProperties.fileName() + "_SAVED", network);
    LOGGER.debug("Réseau calculé : {}{}", System.lineSeparator(), networkResults);

    computeGraphics(network, dataSet, dataSetProperties);
    return true;
  }

  private void computeGraphics(
      BasicNetwork network, MLDataSet dataSet, DataSetProperties dataSetProperties) {
    double xDenormalizedLowerBound = dataSetProperties.xInterval().minValue();
    double xDenormalizedUpperBound = dataSetProperties.xInterval().maxValue();
    double yDenormalizedLowerBound = dataSetProperties.yInterval().minValue();
    double yDenormalizedUpperBound = dataSetProperties.yInterval().maxValue();
    double normalizedLowerBound = dataSetProperties.normalizedInterval().minValue();
    double normalizedUpperBound = dataSetProperties.normalizedInterval().maxValue();

    DataNormalizer xProjection =
        DataNormalizer.toInterval(xDenormalizedLowerBound, xDenormalizedUpperBound)
            .fromInterval(normalizedLowerBound, normalizedUpperBound);

    DataNormalizer yProjection =
        DataNormalizer.toInterval(yDenormalizedLowerBound, yDenormalizedUpperBound)
            .fromInterval(normalizedLowerBound, normalizedUpperBound);

    double sumNormDiffPercentage = 0;
    double maxNormDiffPercentage = 0;

    XYGraphicDisplay graphics = initGraphics(dataSetProperties);

    List<Double> x = Stream.iterate(0d, i -> i + 1).limit(dataSet.getRecordCount()).toList();
    List<Double> y = new ArrayList<>();
    List<Double> yPredicted = new ArrayList<>();
    for (MLDataPair actualInputAndOutput : dataSet) {
      // Les données ci-dessous sont normalisées sur l'intervalle normalisé fourni :
      // [-1, 1] en général
      MLData inputData = actualInputAndOutput.getInput();
      MLData outputData = actualInputAndOutput.getIdeal();
      MLData predictedOutput = network.compute(inputData);

      List<Double> xNormalizedValue = DoubleStream.of(inputData.getData()).boxed().toList();
      double yNormalizedValue = outputData.getData(0);
      double yNormalizedPredictedValue = predictedOutput.getData(0);

      List<Double> xDenormalizedValue = xProjection.normalize(xNormalizedValue);
      double yDenormalizedValue = yProjection.normalize(yNormalizedValue).getFirst();
      double yDenormalizedPredictedValue =
          yProjection.normalize(yNormalizedPredictedValue).getFirst();

      double valueDifference =
          Math.abs(
              ((yDenormalizedValue - yDenormalizedPredictedValue) / yDenormalizedValue) * 100.0);

      LOGGER.debug(
          "X = {}, Predicted Y = {} (difference = {})",
          xDenormalizedValue,
          yDenormalizedPredictedValue,
          valueDifference);

      sumNormDiffPercentage += valueDifference;
      if (Double.compare(valueDifference, maxNormDiffPercentage) > 0) {
        maxNormDiffPercentage = valueDifference;
      }
      y.add(yDenormalizedValue);
      yPredicted.add(yDenormalizedPredictedValue);
    }

    LOGGER.debug(
        "Sum normalized differences = {}, Max normalized difference = {})",
        sumNormDiffPercentage,
        maxNormDiffPercentage);

    graphics.addXYSerie(
        "Actual data", x, y, new XYSerieProperties(XChartSeriesColors.BLUE, SeriesLines.SOLID));
    graphics.addXYSerie(
        "Predicted data", x, yPredicted, new XYSerieProperties(Color.ORANGE, SeriesLines.SOLID));

    exportAndDisplay(graphics);
  }

  private BasicNetwork createNetwork(MLDataSet dataSet) {
    BasicNetwork network = new BasicNetwork();
    if (dataSet == null) {
      return network;
    }
    int inputSize = dataSet.getInputSize();
    int outputSize = dataSet.getIdealSize();

    // La couche INPUT
    createInputLayer(network, inputSize);
    // 7 couches cachées de 60 neurones chacune
    createHiddenLayer(network, 7, 60);
    // La couche OUTPUT
    createOutputLayer(network, outputSize);

    initNetwork(network);

    return network;
  }

  private void initNetwork(BasicNetwork network) {
    network.getStructure().finalizeStructure();
    network.reset();
  }

  private void createInputLayer(BasicNetwork network, int neuronCount) {
    network.addLayer(new BasicLayer(NO_ACTIVATION, HAS_BIAS, neuronCount));
  }

  private void createHiddenLayer(BasicNetwork network, int layerCount, int neuronCountPerLayer) {
    for (int i = 0; i < layerCount; i++) {
      network.addLayer(new BasicLayer(DEFAULT_ACTIVATION, HAS_BIAS, neuronCountPerLayer));
    }
  }

  private void createOutputLayer(BasicNetwork network, int neuronCount) {
    network.addLayer(new BasicLayer(DEFAULT_ACTIVATION, NO_BIAS, neuronCount));
  }
}
