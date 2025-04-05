package fr.nico.neural.network.djl.multilayerperceptrons.fromscratch;

import static fr.nico.neural.network.djl.linearneuralnetworks.linearregression.fromscratch.LinearRegression.stochasticGradientDescent;
import static fr.nico.neural.network.djl.linearneuralnetworks.softmaxregession.concise.SoftmaxRegression.BATCH_SIZE;
import static fr.nico.neural.network.djl.linearneuralnetworks.softmaxregession.concise.SoftmaxRegression.trainingSet;
import static fr.nico.neural.network.djl.linearneuralnetworks.softmaxregession.concise.SoftmaxRegression.validationSet;
import static fr.nico.neural.network.djl.linearneuralnetworks.softmaxregession.fromscratch.SoftmaxRegression.accuracy;
import static java.util.Objects.requireNonNull;

import ai.djl.engine.Engine;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.GradientCollector;
import ai.djl.training.dataset.Batch;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.loss.Loss;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.LinePlot;

@Slf4j
public class MultilayerPerceptron {

  // Une image a une taille de 28x28 soit 784 valeurs de pixel (noirs ou blancs)
  private static final int INPUTS_COUNT = 28 * 28;
  // 10 catégories de vêtements possibles
  private static final int OUTPUTS_COUNT = 10;
  // Le nombre de neurones dans les ('la' en l'occurence) couche(s) cachée(s)
  private static final int HIDDEN_LAYER_NEURONS_COUNT = 256;
  private static final int EPOCHS_COUNT = Integer.getInteger("MAX_EPOCH", 10);
  private static final float LEARNING_RATE = 0.5f;

  private final NDManager manager;
  private final double[] trainLoss = new double[EPOCHS_COUNT];
  private final double[] testAccuracy = new double[EPOCHS_COUNT];
  private final double[] currentEpochCount = new double[EPOCHS_COUNT];
  private final double[] trainAccuracy = new double[EPOCHS_COUNT];

  private NDArray w1Matrix;
  private NDArray w2Matrix;
  private NDArray b1Matrix;
  private NDArray b2Matrix;
  private NDList parameters;

  private MultilayerPerceptron(NDManager manager) {
    this.manager = requireNonNull(manager);
  }

  public static void main(String[] args) throws TranslateException, IOException {
    try (NDManager manager = NDManager.newBaseManager()) {
      MultilayerPerceptron network = new MultilayerPerceptron(manager);

      network.initializeWeightAndBiasParameters();
      network.train();
      network.printResults();
    }
  }

  private void initializeWeightAndBiasParameters() {
    w1Matrix = randomWeightsMatrix(INPUTS_COUNT, HIDDEN_LAYER_NEURONS_COUNT);
    w2Matrix = randomWeightsMatrix(HIDDEN_LAYER_NEURONS_COUNT, OUTPUTS_COUNT);
    b1Matrix = zeroBias(HIDDEN_LAYER_NEURONS_COUNT);
    b2Matrix = zeroBias(OUTPUTS_COUNT);

    parameters = new NDList(w1Matrix, b1Matrix, w2Matrix, b2Matrix);
    parameters.forEach(parameter -> parameter.setRequiresGradient(true));
  }

  private NDArray randomWeightsMatrix(int rowCount, int colCount) {
    return manager.randomNormal(0, 0.01f, new Shape(rowCount, colCount), DataType.FLOAT32);
  }

  private NDArray zeroBias(int rowCount) {
    return manager.zeros(new Shape(rowCount));
  }

  private NDArray model(NDArray input) {
    // La matrice 'input' passée va être "reshapée"
    // en matrice qui aura obligatoirement 'INPUTS_COUNT' colonnes
    // et adaptera le nombre de lignes en fonction.
    // EX: si INPUTS_COUNT = 784 et qu'on reçoit un array de shape 256x1x28x28
    // on va obtenir une matrice input : 256x784
    input = input.reshape(new Shape(-1, INPUTS_COUNT));
    // hidden : 256x784 . 784x256 + 256x1 (broadcastée) => hidden : 256x256
    NDArray hidden = input.dot(w1Matrix).add(b1Matrix);
    NDArray reluHidden = relu(hidden); // apply activation function
    // output : 256x256 . 256x10 + 10x1 (broadcastée) => output : 256x10
    NDArray output = reluHidden.dot(w2Matrix).add(b2Matrix);
    log.debug("input {}, ReLU(hidden) {}, output {}", input, reluHidden, output);
    return output;
  }

  private void train() throws TranslateException, IOException {
    float epochLoss = 0f;
    float accuracyVal = 0f;

    RandomAccessDataset trainingSet = trainingSet(); // taille des batchs : 256 éléments
    RandomAccessDataset validationSet = validationSet(true);
    for (int epoch = 1; epoch <= EPOCHS_COUNT; epoch++) {
      log.info("Running epoch {} ...... ", epoch);
      // Iterate over dataset
      for (Batch batch : trainingSet.getData(manager)) {
        // x : 256x1x28x28
        NDArray x = batch.getData().head();
        // y : 256x1
        NDArray y = batch.getLabels().head();

        try (GradientCollector gc = Engine.getInstance().newGradientCollector()) {
          // yHat : 256x10
          NDArray yHat = model(x);
          NDArray lossValue =
              Loss.softmaxCrossEntropyLoss().evaluate(new NDList(y), new NDList(yHat));
          NDArray l = lossValue.mul(BATCH_SIZE);
          accuracyVal += accuracy(yHat, y);
          epochLoss += l.sum().getFloat();
          gc.backward(l); // gradient calculation
        }
        batch.close();

        // Mise à jour des poids et biais
        stochasticGradientDescent(parameters, LEARNING_RATE, BATCH_SIZE);
      }
      trainLoss[epoch - 1] = epochLoss / trainingSet.size();
      trainAccuracy[epoch - 1] = accuracyVal / trainingSet.size();

      epochLoss = 0f;
      accuracyVal = 0f;
      // testing now
      for (Batch batch : validationSet.getData(manager)) {
        NDArray x = batch.getData().head();
        NDArray y = batch.getLabels().head();
        NDArray yHat = model(x); // net function call
        accuracyVal += accuracy(yHat, y);
      }

      testAccuracy[epoch - 1] = accuracyVal / validationSet.size();
      currentEpochCount[epoch - 1] = epoch;
      accuracyVal = 0f;
      log.info("Finished epoch {}", epoch);
    }
    log.info("Finished training!");
  }

  public void printResults() {
    String[] lossLabel = new String[trainLoss.length + testAccuracy.length + trainAccuracy.length];

    Arrays.fill(lossLabel, 0, trainLoss.length, "train loss");
    Arrays.fill(
        lossLabel, trainAccuracy.length, trainLoss.length + trainAccuracy.length, "train acc");
    Arrays.fill(
        lossLabel,
        trainLoss.length + trainAccuracy.length,
        trainLoss.length + testAccuracy.length + trainAccuracy.length,
        "test acc");

    Table data =
        Table.create("Data")
            .addColumns(
                DoubleColumn.create(
                    "epochCount",
                    ArrayUtils.addAll(
                        currentEpochCount,
                        ArrayUtils.addAll(currentEpochCount, currentEpochCount))),
                DoubleColumn.create(
                    "loss",
                    ArrayUtils.addAll(trainLoss, ArrayUtils.addAll(trainAccuracy, testAccuracy))),
                StringColumn.create("lossLabel", lossLabel));

    Plot.show(LinePlot.create("", data, "epochCount", "loss", "lossLabel"));
  }

  /**
   * Fonction d'activation ReLU
   *
   * @param z la valeur
   * @return max(0, z)
   */
  private static NDArray relu(NDArray z) {
    return z.maximum(0f);
  }
}
