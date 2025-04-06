package fr.nico.neural.network.djl.multilayerperceptrons.concise;

import static fr.nico.neural.network.djl.linearneuralnetworks.softmaxregession.concise.SoftmaxRegression.trainingSet;
import static fr.nico.neural.network.djl.linearneuralnetworks.softmaxregession.concise.SoftmaxRegression.validationSet;

import ai.djl.Model;
import ai.djl.engine.Engine;
import ai.djl.metric.Metric;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.Parameter;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingConfig;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.initializer.NormalInitializer;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.tracker.Tracker;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import org.apache.commons.lang3.ArrayUtils;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.LinePlot;

public class MultilayerPerceptron {

  private static final int INPUTS_COUNT = 28 * 28;
  private static final int OUTPUTS_COUNT = 10;
  private static final int HIDDEN_LAYER_NEURONS_COUNT = 256;
  private static final int EPOCHS_COUNT = Integer.getInteger("MAX_EPOCH", 10);
  private static final float LEARNING_RATE = 0.5f;
  private static final UnaryOperator<NDList> ACTIVATION_FUNCTION = Activation::relu;
  private static final Tracker LEARNING_RATE_TRACKER = Tracker.fixed(LEARNING_RATE);
  private static final Optimizer STOCHASTIC_GRADIENT_DESCENT =
      Optimizer.sgd().setLearningRateTracker(LEARNING_RATE_TRACKER).build();
  private static final Loss CROSS_ENTROPY_LOSS = Loss.softmaxCrossEntropyLoss();
  private static final double[] EPOCHS =
      IntStream.rangeClosed(1, EPOCHS_COUNT).asDoubleStream().toArray();
  private static final String TRAIN_ACCURACY_EVALUATOR_NAME = "train_epoch_Accuracy";
  private static final String TRAIN_LOSS_EVALUATOR_NAME = "train_epoch_SoftmaxCrossEntropyLoss";
  private static final String VALIDATION_ACCURACY_EVALUATOR_NAME = "validate_epoch_Accuracy";

  private final Map<String, double[]> evaluatorMetrics;

  private RandomAccessDataset trainingSet;
  private RandomAccessDataset validationSet;

  private MultilayerPerceptron() {
    this.evaluatorMetrics = new HashMap<>();
  }

  public static void main(String[] args) throws TranslateException, IOException {
    MultilayerPerceptron network = new MultilayerPerceptron();
    network.train();
    network.print();
  }

  private void train() throws TranslateException, IOException {
    prepareDataSets();
    TrainingConfig configuration = createNetworkTrainingConfiguration();
    SequentialBlock network = createNetwork();

    try (Model model = Model.newInstance("mlp")) {
      model.setBlock(network);

      try (Trainer trainer = model.newTrainer(configuration)) {

        trainer.initialize(new Shape(1, INPUTS_COUNT));
        trainer.setMetrics(new Metrics());

        EasyTrain.fit(trainer, EPOCHS_COUNT, this.trainingSet, this.validationSet);
        // collect results from evaluators
        populatePrintMetrics(trainer.getMetrics());
      }
    }
  }

  private void prepareDataSets() throws TranslateException, IOException {
    this.trainingSet = trainingSet();
    this.validationSet = validationSet(true);

    trainingSet.prepare();
    validationSet.prepare();
  }

  private void print() {
    double[] trainLoss = evaluatorMetrics.get(TRAIN_LOSS_EVALUATOR_NAME);
    double[] trainAccuracy = evaluatorMetrics.get(TRAIN_ACCURACY_EVALUATOR_NAME);
    double[] testAccuracy = evaluatorMetrics.get(VALIDATION_ACCURACY_EVALUATOR_NAME);

    String[] lossLabel = new String[trainLoss.length + testAccuracy.length + trainAccuracy.length];

    Arrays.fill(lossLabel, 0, trainLoss.length, "test acc");
    Arrays.fill(
        lossLabel, trainAccuracy.length, trainLoss.length + trainAccuracy.length, "train acc");
    Arrays.fill(
        lossLabel,
        trainLoss.length + trainAccuracy.length,
        trainLoss.length + testAccuracy.length + trainAccuracy.length,
        "train loss");

    Table data =
        Table.create("Data")
            .addColumns(
                DoubleColumn.create(
                    "epochCount", ArrayUtils.addAll(EPOCHS, ArrayUtils.addAll(EPOCHS, EPOCHS))),
                DoubleColumn.create(
                    "loss",
                    ArrayUtils.addAll(testAccuracy, ArrayUtils.addAll(trainAccuracy, trainLoss))),
                StringColumn.create("lossLabel", lossLabel));

    Plot.show(LinePlot.create("", data, "epochCount", "loss", "lossLabel"));
  }

  private static TrainingConfig createNetworkTrainingConfiguration() {
    return new DefaultTrainingConfig(CROSS_ENTROPY_LOSS)
        .optOptimizer(STOCHASTIC_GRADIENT_DESCENT)
        .optDevices(Engine.getInstance().getDevices(1)) // single GPU
        .addEvaluator(new Accuracy()) // Model Accuracy
        .addTrainingListeners(TrainingListener.Defaults.logging()); // Logging
  }

  private static SequentialBlock createNetwork() {
    SequentialBlock net = new SequentialBlock();
    net.add(Blocks.batchFlattenBlock(INPUTS_COUNT));
    net.add(Linear.builder().setUnits(HIDDEN_LAYER_NEURONS_COUNT).build());
    net.add(ACTIVATION_FUNCTION);
    net.add(Linear.builder().setUnits(OUTPUTS_COUNT).build());
    net.setInitializer(new NormalInitializer(), Parameter.Type.WEIGHT);

    return net;
  }

  private void populatePrintMetrics(Metrics metrics) {
    evaluatorMetrics.put(
        TRAIN_ACCURACY_EVALUATOR_NAME, extractMetricsFor(metrics, TRAIN_ACCURACY_EVALUATOR_NAME));
    evaluatorMetrics.put(
        TRAIN_LOSS_EVALUATOR_NAME, extractMetricsFor(metrics, TRAIN_LOSS_EVALUATOR_NAME));
    evaluatorMetrics.put(
        VALIDATION_ACCURACY_EVALUATOR_NAME,
        extractMetricsFor(metrics, VALIDATION_ACCURACY_EVALUATOR_NAME));
  }

  private static double[] extractMetricsFor(Metrics metrics, String evaluatorName) {
    return metrics.getMetric(evaluatorName).stream().mapToDouble(Metric::getValue).toArray();
  }
}
