package fr.nico.neural.network.djl.multilayerperceptrons.dropout.concise;

import static fr.nico.neural.network.djl.linearneuralnetworks.softmaxregession.concise.SoftmaxRegression.trainingSet;
import static fr.nico.neural.network.djl.linearneuralnetworks.softmaxregession.concise.SoftmaxRegression.validationSet;
import static fr.nico.neural.network.djl.multilayerperceptrons.concise.MultilayerPerceptron.TRAIN_ACCURACY_EVALUATOR_NAME;
import static fr.nico.neural.network.djl.multilayerperceptrons.concise.MultilayerPerceptron.TRAIN_LOSS_EVALUATOR_NAME;
import static fr.nico.neural.network.djl.multilayerperceptrons.concise.MultilayerPerceptron.VALIDATION_ACCURACY_EVALUATOR_NAME;

import ai.djl.Model;
import ai.djl.engine.Engine;
import ai.djl.metric.Metric;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Activation;
import ai.djl.nn.Blocks;
import ai.djl.nn.Parameter;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.nn.norm.Dropout;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
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
import java.util.stream.IntStream;
import org.apache.commons.lang3.ArrayUtils;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.LinePlot;

public class DropoutImplementation {

  private static final int EPOCHS_COUNT = Integer.getInteger("MAX_EPOCH", 10);

  private static final double[] EPOCHS =
      IntStream.rangeClosed(1, EPOCHS_COUNT).asDoubleStream().toArray();

  public static void main(String[] args) throws TranslateException, IOException {
    //    Pendant l'apprentissage, la couche d'exclusion élimine de manière aléatoire
    //    les sorties de la couche précédente (ou de manière équivalente les entrées de la couche
    // suivante)
    //    en fonction de la probabilité d'exclusion spécifiée.
    //    Lorsque le modèle n'est pas en mode d'apprentissage,
    //    la couche Dropout transmet simplement les données lors des tests.
    SequentialBlock net = new SequentialBlock();
    net.add(Blocks.batchFlattenBlock(784));
    net.add(Linear.builder().setUnits(256).build());
    net.add(Activation::relu);
    net.add(
        Dropout.builder()
            .optRate(0.2f) // 1 chance sur 5 de désactiver les neurones de la couche
            .build());
    net.add(Linear.builder().setUnits(256).build());
    net.add(Activation::relu);
    net.add(
        Dropout.builder()
            .optRate(0.5f) // 1 chance sur 2 de désactiver les neurones de la couche
            .build());
    net.add(Linear.builder().setUnits(10).build());
    net.setInitializer(new NormalInitializer(0.01f), Parameter.Type.WEIGHT);

    Map<String, double[]> evaluatorMetrics = new HashMap<>();

    Tracker learningRate = Tracker.fixed(0.5f);
    Optimizer stochasticGradientDescentOptimizer =
        Optimizer.sgd().setLearningRateTracker(learningRate).build();
    DefaultTrainingConfig config =
        new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
            .optOptimizer(stochasticGradientDescentOptimizer)
            .optDevices(Engine.getInstance().getDevices(1)) // single GPU
            .addEvaluator(new Accuracy())
            .addTrainingListeners(TrainingListener.Defaults.logging());

    try (Model model = Model.newInstance("mlp")) {
      model.setBlock(net);

      try (Trainer trainer = model.newTrainer(config)) {

        trainer.initialize(new Shape(1, 784));
        trainer.setMetrics(new Metrics());

        RandomAccessDataset trainingSet = trainingSet();
        RandomAccessDataset validationSet = validationSet(true);

        trainingSet.prepare();
        validationSet.prepare();
        EasyTrain.fit(trainer, EPOCHS_COUNT, trainingSet, validationSet);

        Metrics metrics = trainer.getMetrics();

        trainer
            .getEvaluators()
            .forEach(
                evaluator -> {
                  evaluatorMetrics.put(
                      "train_epoch_" + evaluator.getName(),
                      metrics.getMetric("train_epoch_" + evaluator.getName()).stream()
                          .mapToDouble(Metric::getValue)
                          .toArray());
                  evaluatorMetrics.put(
                      "validate_epoch_" + evaluator.getName(),
                      metrics.getMetric("validate_epoch_" + evaluator.getName()).stream()
                          .mapToDouble(Metric::getValue)
                          .toArray());
                });

        print(evaluatorMetrics);
      }
    }
  }

  private static void print(Map<String, double[]> evaluatorMetrics) {
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
}
