package fr.nico.neural.network.djl.linearneuralnetworks.softmaxregressionfromscratch;

import static fr.nico.neural.network.djl.linearneuralnetworks.imageclassificationdataset.MNISTDataset.getFashionMnistLabels;

import ai.djl.basicdataset.cv.classification.FashionMnist;
import ai.djl.engine.Engine;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.GradientCollector;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.Batch;
import ai.djl.training.dataset.Dataset;
import ai.djl.translate.TranslateException;
import fr.nico.neural.network.djl.Animator;
import fr.nico.neural.network.djl.linearneuralnetworks.imageclassificationdataset.MNISTDataset;
import fr.nico.neural.network.djl.linearneuralnetworks.linearregressionfromscratch.LinearRegression;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SoftmaxRegressionFromScratch {

  private static final int[] SUM_ON_COL_AXIS = {
    1
  }; // dimension '1' (col) est fixe donc on sommera chaque ligne sur 1 colonne.
  private static final int[] SUM_ON_ROW_AXIS = {
    0
  }; // dimension '0' (row) est fixe donc on sommera chaque colonne sur 1 ligne

  public static void main(String[] args) throws TranslateException, IOException {
    // Les images du dataset sont des carrés de 28 pixels par 28 pixels
    // ce qui donne mis à plat des vecteurs de 28 * 28 = 784 valeurs
    int inputCount = 784;
    // Notre dataset de Zalando a 10 catégories de vêtements
    int outputCount = 10;

    int batchSize = 256;
    boolean randomShuffle = true;

    // get training and validation dataset
    FashionMnist trainingSet =
        FashionMnist.builder()
            .optUsage(Dataset.Usage.TRAIN)
            .setSampling(batchSize, randomShuffle)
            .optLimit(Long.getLong("DATASET_LIMIT", Long.MAX_VALUE))
            .build();

    FashionMnist validationSet =
        FashionMnist.builder()
            .optUsage(Dataset.Usage.TEST)
            .setSampling(batchSize, false)
            .optLimit(Long.getLong("DATASET_LIMIT", Long.MAX_VALUE))
            .build();

    // Les poids sont une matrice 784 x 10
    NDArray w;
    // Les biais sont une matrice 1 x 10 (un vecteur)
    NDArray b;
    try (NDManager manager = NDManager.newBaseManager()) {
      testSum(manager);
      testArgMax(manager);

      w = manager.randomNormal(0, 0.01f, new Shape(inputCount, outputCount), DataType.FLOAT32);
      b = manager.zeros(new Shape(outputCount), DataType.FLOAT32);

      NDList params = new NDList(w, b);

      int numEpochs = 10;
      float learningRate = 0.1f;
      Animator animator = new Animator();
      Net net = new Net(params, inputCount);
      for (int i = 1; i <= numEpochs; i++) {
        float[] trainMetrics =
            trainEpochCh3(
                net::net,
                trainingSet.getData(manager),
                SoftmaxRegressionFromScratch::crossEntropy,
                LinearRegression::stochasticGradientDescent,
                params,
                inputCount,
                learningRate);
        float accuracy = evaluateAccuracy(net::net, validationSet.getData(manager));
        float trainAccuracy = trainMetrics[1];
        float trainLoss = trainMetrics[0];

        animator.add(i, accuracy, trainAccuracy, trainLoss);
        log.info("Epoch {}: Test Accuracy: {}", i, accuracy);
        log.info("Train Accuracy: {}", trainAccuracy);
        log.info("Train Loss: {}", trainLoss);
      }

      predictCh3(net::net, validationSet, 6, manager);
    }
  }

  /**
   * @param x
   * @return softmax(X, i, j) = exp(Xij) / ∑(exp(Xik), k = 1..dim(j)).
   */
  public static NDArray softmax(NDArray x) {
    boolean keepDims = true;
    NDArray xExp = x.exp();
    NDArray partition = xExp.sum(SUM_ON_COL_AXIS, keepDims);
    return xExp.div(partition); // The broadcast mechanism is applied here
  }

  /**
   * Loss function (cross entropy)
   *
   * @param yHat the predicted value, y^ = softmax(Output) = softmax(XW+b)
   * @param y the actual value (label)
   * @return l = −log(P(y∣x)) = −∑(yj*log(y^j), j = 1..n)
   */
  public static NDArray crossEntropy(NDArray yHat, NDArray y) {
    // Here, y is not guranteed to be of datatype int or long
    // and in our case we know its a float32.
    // We must first convert it to int or long (here we choose int)
    // before we can use it with NDIndex to "pick" indices.
    // It also takes in a boolean for returning a copy of the existing NDArray
    // but we don't want that so we pass in `false`.
    int count = Math.floorMod(-1, yHat.getShape().dimension());
    NDIndex pickIndex = new NDIndex().addAllDim(count).addPickDim(y);
    return yHat.get(pickIndex).log().neg();
  }

  public static float accuracy(NDArray yHat, NDArray y) {
    // Check size of 1st dimension greater than 1
    // to see if we have multiple samples
    if (yHat.getShape().size(1) > 1) {
      // Argmax gets index of maximum args for given axis 1
      // Convert yHat to same dataType as y (int32)
      // Sum up number of true entries
      int columnAxis = 1;
      return yHat.argMax(columnAxis)
          .toType(DataType.INT32, false)
          .eq(y.toType(DataType.INT32, false))
          .sum()
          .toType(DataType.FLOAT32, false)
          .getFloat();
    }
    return yHat.toType(DataType.INT32, false)
        .eq(y.toType(DataType.INT32, false))
        .sum()
        .toType(DataType.FLOAT32, false)
        .getFloat();
  }

  public static float evaluateAccuracy(UnaryOperator<NDArray> net, Iterable<Batch> dataIterator) {
    Accumulator metric = new Accumulator(2); // numCorrectedExamples, numExamples
    Batch batch = dataIterator.iterator().next();
    NDArray x = batch.getData().head();
    NDArray y = batch.getLabels().head();
    metric.add(new float[] {accuracy(net.apply(x), y), y.size()});
    batch.close();

    return metric.get(0) / metric.get(1);
  }

  public static float[] trainEpochCh3(
      UnaryOperator<NDArray> net,
      Iterable<Batch> trainIter,
      BinaryOperator<NDArray> loss,
      ParamConsumer parametersUpdater,
      NDList params,
      int inputCount,
      float learningRate) {
    Accumulator metric = new Accumulator(3); // trainLossSum, trainAccSum, numExamples

    // Attach Gradients
    for (NDArray param : params) {
      param.setRequiresGradient(true);
    }

    for (Batch batch : trainIter) {
      NDArray x = batch.getData().head();
      NDArray y = batch.getLabels().head();
      x = x.reshape(new Shape(-1, inputCount));

      try (GradientCollector gc = Engine.getInstance().newGradientCollector()) {
        // Minibatch loss in X and y
        NDArray yHat = net.apply(x);
        NDArray l = loss.apply(yHat, y);
        gc.backward(l); // Compute gradient on l with respect to w and b
        metric.add(
            new float[] {
              l.sum().toType(DataType.FLOAT32, false).getFloat(), accuracy(yHat, y), y.size()
            });
      }
      parametersUpdater.accept(
          params, learningRate, batch.getSize()); // Update parameters using their gradient

      batch.close();
    }
    // Return trainLoss, trainAccuracy
    return new float[] {metric.get(0) / metric.get(2), metric.get(1) / metric.get(2)};
  }

  public static void predictCh3(
      UnaryOperator<NDArray> net, ArrayDataset dataset, int number, NDManager manager)
      throws IOException, TranslateException {
    int[] predLabels = new int[number];

    Batch batch = dataset.getData(manager).iterator().next();
    NDArray x = batch.getData().head();
    int[] yHat = net.apply(x).argMax(1).toType(DataType.INT32, false).toIntArray();
    System.arraycopy(yHat, 0, predLabels, 0, number);
    String[] fashionMnistLabels = getFashionMnistLabels(predLabels);
    log.info("Predicted labels : {}", (Object) fashionMnistLabels);

    MNISTDataset.showImages(dataset, number, 28, 28, 4, manager);
  }

  public static class Net {

    private final NDList params;
    private final int inputCount;

    public Net(NDList params, int inputCount) {
      this.params = params;
      this.inputCount = inputCount;
    }

    public NDArray net(NDArray x) {
      NDArray currentW = params.get(0);
      NDArray currentB = params.get(1);
      return softmax(x.reshape(new Shape(-1, inputCount)).dot(currentW).add(currentB));
    }
  }

  public static class Accumulator {
    float[] data;

    public Accumulator(int n) {
      data = new float[n];
    }

    /* Adds a set of numbers to the array */
    public void add(float[] args) {
      for (int i = 0; i < args.length; i++) {
        data[i] += args[i];
      }
    }

    /* Resets the array */
    public void reset() {
      Arrays.fill(data, 0f);
    }

    /* Returns the data point at the given index */
    public float get(int index) {
      return data[index];
    }
  }

  @FunctionalInterface
  public static interface ParamConsumer {
    void accept(NDList params, float learningRate, int batchSize);
  }

  private static void testSum(NDManager manager) {
    // test of sum method on row axis (with keepDims option)
    NDArray matrix = manager.create(new int[][] {{1, 2, 3}, {4, 5, 6}, {1, 3, 5}});
    // M = [[ 1,  2,  3],
    //      [ 4,  5,  6],
    //      [ 1,  3,  5],
    //     ]
    log.info("M : {} =  {}", matrix, matrix.sum(SUM_ON_COL_AXIS, false));
    // Somme sans conserver les dimensions = vecteur à 1 dimension :
    // [ 6, 15,  9]
    log.info("M : {} =  {}", matrix, matrix.sum(SUM_ON_COL_AXIS, true));
    // Somme en conservant les dimensions = matrice 3 x 1 :
    // [[ 6],
    //  [15],
    //  [ 9],
    // ]

    log.info("M : {} =  {}", matrix, matrix.sum(SUM_ON_ROW_AXIS, false));
    // [ 6, 10, 14]
    log.info("M : {} =  {}", matrix, matrix.sum(SUM_ON_ROW_AXIS, true));
    // [[ 6, 10, 14],
    // ]
  }

  private static void testArgMax(NDManager manager) {
    // test of argmax method on row axis
    NDArray matrix = manager.create(new int[][] {{1, -1, 0, -1}, {2, 2, 3, 1}, {4, 0, 5, -1}});
    // M = [[ 1,  -1, 0, -1],
    //      [ 2,  2,  3,  1],
    //      [ 4,  0,  5, -1],
    //     ]

    int rowAxis = 0;
    int colAxis = 1;
    // On regarde pour chaque colonne quel index de ligne a la plus grande valeur
    log.info("ArgMax(0) = {}", matrix.argMax(rowAxis));
    // [ 2,  1,  2,  1]

    // On regarde pour chaque ligne quel index de colonne a la plus grande valeur
    log.info("ArgMax(1) = {}", matrix.argMax(colAxis));
    // [ 0,  2,  2]
  }
}
