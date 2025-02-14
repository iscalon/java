package fr.nico.neural.network.four.application;

import static java.util.Objects.requireNonNull;

import fr.nico.neural.network.one.application.out.DataRepository;
import fr.nico.neural.network.one.application.shared.DataSetProperties;
import org.encog.engine.network.activation.ActivationFunction;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NeuralNetworkFourTraining {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeuralNetworkFourTraining.class);

  private static final ActivationFunction NO_ACTIVATION = null;
  private static final ActivationFunction DEFAULT_ACTIVATION = new ActivationTANH();
  private static final boolean HAS_BIAS = true;
  private static final boolean NO_BIAS = false;
  private static final double EXPECTED_ERROR_RATE = 0.0000000000000000012;
  private static final double DELTA = 0.0000000000000000001;
  private static final int MAXIMUM_TRAIN_LOOP_COUNT = 1000000;

  private final DataRepository<BasicNetwork> networks;

  NeuralNetworkFourTraining(DataRepository<BasicNetwork> networks) {
    this.networks = requireNonNull(networks);
  }

  public void trainAndSaveNetworkUsing(DataSetProperties dataSetProperties) {
    MLDataSet dataSet = networks.readDataSet(dataSetProperties);

    LOGGER.info("Création et entrainement du réseau");
    BasicNetwork network = createNetwork(dataSet);
    trainAndSaveNetwork(network, dataSet, dataSetProperties);
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
          && Double.compare(errorAfterTrain, EXPECTED_ERROR_RATE - DELTA) > 0) {
        LOGGER.warn(
            "L'entrainement du réseau a échoué avec une erreur de {}, peut-être devriez vous relancer l'entrainement ?",
            error);
        return false;
      }

    } while (Double.compare(network.calculateError(dataSet), EXPECTED_ERROR_RATE) > 0);
    LOGGER.info("L'entrainement du réseau a réussi avec une erreur de {}", error);
    return true;
  }

  private void trainAndSaveNetwork(
      BasicNetwork network, MLDataSet dataSet, DataSetProperties dataSetProperties) {

    train(network, dataSet);
    String networkResults = networks.saveNetwork(dataSetProperties.fileName() + "_SAVED", network);
    LOGGER.debug("Réseau calculé : {}{}", System.lineSeparator(), networkResults);
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
    // 2 couches cachées de 3 neurones chacune
    createHiddenLayer(network, 2, 3);
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
