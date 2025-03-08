package fr.nico.neural.network.djl.linearneuralnetworks.conciseimplementation;

import static fr.nico.neural.network.djl.linearneuralnetworks.linearregressionfromscratch.LinearRegression.syntheticData;

import ai.djl.Model;
import ai.djl.metric.Metrics;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.nn.Parameter;
import ai.djl.nn.ParameterList;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.Batch;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.tracker.Tracker;
import ai.djl.translate.TranslateException;
import fr.nico.neural.network.djl.linearneuralnetworks.linearregressionfromscratch.LinearRegression;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConciseImplementation {

  private static final float[] W = {2, -3.4f};
  private static final float B = 4.2f;
  private static final int DATASET_SIZE = 1000;
  private static final int GPU_COUNT = 1;
  private static final boolean NO_SHUFFLE = false;
  private static final String MODEL_NAME = "mon modèle";
  private static final int EPOCHS_COUNT = 3;

  public static void main(String[] args) throws TranslateException, IOException {
    try (NDManager manager = NDManager.newBaseManager()) {
      // ============ Génération des données
      LinearRegression.DataPoints dp = syntheticData(manager, manager.create(W), B, DATASET_SIZE);

      // ============ Définition du modèle
      try (Model model = Model.newInstance(MODEL_NAME)) {

        // Création de la séquence de blocs (i.e. le réseau de neurones)
        SequentialBlock net = new SequentialBlock();

        int outputSize = 1;
        boolean hasBias = true;
        // Nous avons 1 seule couche, les inputs lui seront passés
        //  et nous aurons 'outputSize' sortie pour celle-ci
        net.add(Linear.builder().optBias(hasBias).setUnits(outputSize).build());
        model.setBlock(net);

        // ============ Définition du 'learning rate' : η et de l'algorithme d'optimisation
        // (stochastic gradient descent)
        Tracker learningRateTracker = Tracker.fixed(0.03f);
        Optimizer stochasticGradientDescent =
            Optimizer.sgd().setLearningRateTracker(learningRateTracker).build();

        // ============ Instanciation de la configuration
        // de la fonction de perte (loss) et de l'entraineur
        DefaultTrainingConfig config =
            new DefaultTrainingConfig(Loss.l2Loss()) // la L2Loss est : ∑(yi−yCalculei)^2
                .optOptimizer(stochasticGradientDescent)
                .optDevices(manager.getEngine().getDevices(GPU_COUNT))
                .addTrainingListeners(TrainingListener.Defaults.logging()); // Logging

        NDArray features = dp.getX();
        NDArray labels = dp.getY();
        int batchSize = DATASET_SIZE / 100;
        // 'features' est une matrice de dimension DATASET_SIZEx2
        int inputDimensionIndex = 1;
        int intputSize = (int) features.getShape().size(inputDimensionIndex);
        // ============ Initialisation des paramètres du modèle
        // (poids : 'w' et biais : 'b')
        try (Trainer trainer = model.newTrainer(config)) {
          // Le 1er axe représente la taille de chaque batch - n'a pas d'impact sur l'initialisation
          // des paramètres
          // Le 2ème axe est le nombre d'input
          trainer.initialize(new Shape(batchSize, intputSize));

          // ============ Optionnel : métriques
          trainer.setMetrics(new Metrics());

          // ============ Entrainement
          ArrayDataset dataset =
              new ArrayDataset.Builder()
                  .setData(features)
                  .optLabels(labels)
                  .setSampling(batchSize, NO_SHUFFLE)
                  .build();
          for (int epoch = 1; epoch <= EPOCHS_COUNT; epoch++) {
            for (Batch batch : trainer.iterateDataset(dataset)) {
              EasyTrain.trainBatch(trainer, batch);

              // Mise à jour de 'w' et 'b'
              trainer.step();

              batch.close();
            }
            // Reset training and validation evaluators at end of epoch
            trainer.notifyListeners(listener -> listener.onEpoch(trainer));
          }
        } // trainer
        persistModel(model);
        showWAndB(model, intputSize);
      } // model
    } // manager
  }

  private static void persistModel(Model model) {
    try {
      Path tempDirectory = Files.createTempDirectory(ConciseImplementation.class.getSimpleName());
      model.setProperty("Epoch", Integer.toString(EPOCHS_COUNT));
      model.save(tempDirectory, MODEL_NAME);
      log.info("Model : {}", model);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void showWAndB(Model model, int intputSize) {
    Block layer = model.getBlock();
    ParameterList params = layer.getParameters();
    log.info("Param keys : {}", params.keys());
    try (Parameter wParameter = params.valueAt(0);
        Parameter bParameter = params.valueAt(1)) {
      NDArray w = wParameter.getArray();
      NDArray b = bParameter.getArray();

      try (NDManager manager = NDManager.newBaseManager()) {
        NDArray trueW = manager.create(W, new Shape(intputSize, 1));
        float[] wArray = trueW.sub(w.reshape(trueW.getShape())).toFloatArray();
        log.info("Error in estimating w: [{} {}]", wArray[0], wArray[1]);
        log.info("Error in estimating b: {}", B - b.getFloat());
      }
    }
  }
}
