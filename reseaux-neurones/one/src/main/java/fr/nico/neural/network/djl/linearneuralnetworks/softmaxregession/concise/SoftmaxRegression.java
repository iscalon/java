package fr.nico.neural.network.djl.linearneuralnetworks.softmaxregession.concise;

import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.FashionMnist;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.tracker.Tracker;
import ai.djl.translate.TranslateException;
import fr.nico.neural.network.djl.linearneuralnetworks.linearregression.concise.LinearRegression;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SoftmaxRegression {

  public static final int BATCH_SIZE = 256;

  public static void main(String[] args) throws TranslateException, IOException {
    try (NDManager manager = NDManager.newBaseManager()) {
      try (Model model = Model.newInstance("softmax-regression")) {
        SequentialBlock net = new SequentialBlock();
        int inputSize = 28 * 28;
        Block block =
            Blocks.batchFlattenBlock(
                inputSize); // The size of input to the block returned must be :
        // batch_size * inputSize.
        net.add(block);
        net.add(Linear.builder().setUnits(10).build()); // set 10 output channels

        model.setBlock(net);

        float learningRate = 0.1f;
        Tracker learningRateTracker = Tracker.fixed(learningRate);
        Optimizer stochasticGradientDescent =
            Optimizer.sgd().setLearningRateTracker(learningRateTracker).build();

        DefaultTrainingConfig config =
            new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                .optOptimizer(stochasticGradientDescent)
                .optDevices(manager.getEngine().getDevices(1)) // single GPU
                .addEvaluator(new Accuracy()) // Model Accuracy
                .addTrainingListeners(TrainingListener.Defaults.logging()); // Logging

        int epochCount = 5;
        try (Trainer trainer = model.newTrainer(config)) {
          trainer.initialize(
              new Shape(
                  BATCH_SIZE /* pas d'influence, voir 'NB' plus bas */,
                  inputSize)); // Input Images are 28 x 28 = 784
          // NB : au final les 'weight' seront de dimension 10 x 784
          // et les 'biais' de dimension 10 x 1
          // W.x + B = 10 x 784 . 784 x 256 + 10 x 1 ---> 10 x 256 + 10 x 1
          // Soit 10 outputs par nombre d'échantillons par batch : 10 x 256

          trainer.setMetrics(new Metrics());

          EasyTrain.fit(trainer, epochCount, trainingSet(), validationSet());
        } // trainer
        persistModel(model, epochCount);
      } // model
    } // manager

    //    Epoch 5 finished.
    //    Train: Accuracy: 0,84, SoftmaxCrossEntropyLoss: 0,49
    //    Validate: Accuracy: 0,83, SoftmaxCrossEntropyLoss: 0,50
    //    forward P50: 0,212 ms, P90: 0,355 ms
    //    training-metrics P50: 0,023 ms, P90: 0,037 ms
    //    backward P50: 0,763 ms, P90: 1,007 ms
    //    step P50: 0,566 ms, P90: 0,905 ms
    //    epoch P50: 0,974 s, P90: 16,202 s
  }

  public static RandomAccessDataset trainingSet() {
    boolean randomShuffle = true;
    return FashionMnist.builder()
        .optUsage(Dataset.Usage.TRAIN)
        .setSampling(BATCH_SIZE, randomShuffle)
        .optLimit(Long.getLong("DATASET_LIMIT", Long.MAX_VALUE))
        .build();
  }

  public static RandomAccessDataset validationSet() {
    return validationSet(false);
  }

  public static RandomAccessDataset validationSet(boolean randomShuffle) {
    return FashionMnist.builder()
        .optUsage(Dataset.Usage.TEST)
        .setSampling(BATCH_SIZE, randomShuffle)
        .optLimit(Long.getLong("DATASET_LIMIT", Long.MAX_VALUE))
        .build();
  }

  private static void persistModel(Model model, int epochCount) {
    try {
      Path tempDirectory = Files.createTempDirectory(LinearRegression.class.getSimpleName());
      model.setProperty("Epoch", Integer.toString(epochCount));
      model.save(tempDirectory, model.getName());
      log.info("Model : {}", model);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
