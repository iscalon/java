package fr.nico.neural.network.djl.linearneuralnetworks.linearregression.fromscratch;

import ai.djl.engine.Engine;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.training.GradientCollector;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.Batch;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.tablesaw.api.FloatColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.ScatterPlot;

/**
 * Dataset de 1000 exemples, chacun possédant 2 caractéristiques échantillonnées depuis une
 * distribution normale standard : X ∈ R1000×2.
 *
 * <p>Les vrais paramètres générant nos données seront w=[2,-3.4]⊤ et b=4.2 et nos étiquettes
 * synthétiques seront attribuées selon le modèle linéaire suivant avec un terme de bruit ϵ :
 * y=Xw+b+ϵ.
 *
 * <p>On peut considérer que ϵ comme capturant les erreurs de mesure potentielles sur les
 * caractéristiques et les étiquettes. Nous supposerons que les hypothèses standard sont valables et
 * donc que ϵ obéit à une distribution normale avec une moyenne de 0. Pour rendre notre problème
 * plus facile, nous fixerons son écart-type à 0,01.
 */
@Slf4j
public class LinearRegression {

  private static final int MEAN = 0;
  private static final float STANDARD_DEVIATION = 0.01f;
  private static final float[] W = {2, -3.4f};
  private static final float B = 4.2f;
  private static final int DATASET_SIZE = 1000;

  public static void main(String[] args) throws TranslateException, IOException {
    try (NDManager manager = NDManager.newBaseManager()) {

      DataPoints dp = syntheticData(manager, manager.create(W), B, DATASET_SIZE);
      NDArray features = dp.getX();
      NDArray labels = dp.getY();

      int batchSize = 10;
      /*
      log.info(
          "Exemple de caractéristiques : [{}, {}]",
          features.get(0).getFloat(0),
          features.get(0).getFloat(1));
      log.info("Exemple de label associé : {}", labels.getFloat(0));

      // 1ère caractéristique vs y
      scatterPlot(features.get(new NDIndex(":, 0")).toFloatArray(), labels.toFloatArray());
      // 2ème caractéristique vs y
      scatterPlot(features.get(new NDIndex(":, 1")).toFloatArray(), labels.toFloatArray());

      // ========== Lecture du dataset

      ArrayDataset dataset =
          new ArrayDataset.Builder()
              .setData(features) // Set the Features
              .optLabels(labels) // Set the Labels
              .setSampling(batchSize, false) // set the batch size and random sampling to false
              .build();

      Batch batch = dataset.getData(manager).iterator().next();
      // Call head() to get the first NDArray
      NDArray x = batch.getData().head();
      NDArray y = batch.getLabels().head();
      // X ∈ R10×2
      // y ∈ R1×10
      // Don't forget to close the batch!
      batch.close();
      */

      // ============ Initialisation des paramètres du modèle (b et w)
      NDArray w = manager.randomNormal(MEAN, STANDARD_DEVIATION, new Shape(2, 1), DataType.FLOAT32);
      // w: [[0.0013],
      //     [0.0072],
      //    ]
      NDArray b = manager.zeros(new Shape(1));
      // b: [0]
      NDList params = new NDList(w, b);

      // =================== Entrainement
      train(manager, features, labels, params, batchSize);
    }
  }

  /**
   * Algo d'optimisation de l'erreur :
   *
   * <p>w ← w−η/|B|*∂w(loss(w,b))
   *
   * <p>b ← b−η/|B|*∂b(loss(w,b))
   *
   * <p>(η = learning rate, |B| = batch size)
   */
  public static void stochasticGradientDescent(NDList params, float learninRate, int batchSize) {
    for (NDArray param : params) {
      // param = param - (learninRate / batchSize) * param.gradient
      param.subi(param.getGradient().mul(learninRate).div(batchSize));
    }
  }

  /**
   *
   *
   * <ul>
   *   In summary, we will execute the following loop:
   *   <li>Initialize parameters (w,b)
   *   <li>Repeat until done :
   *       <ul>
   *         <li>Compute gradient g←∂(w,b)*1/B∑(loss(xi,yi,w,b))
   *         <li>Update parameters (w,b)←(w,b)−ηu*g
   *       </ul>
   * </ul>
   */
  public static void train(
      NDManager manager, NDArray features, NDArray labels, NDList params, int batchSize)
      throws TranslateException, IOException {
    float learningRate = 0.03f; // nu
    int numEpochs = 3; // Number of Iterations

    ArrayDataset dataset =
        new ArrayDataset.Builder()
            .setData(features) // Set the Features
            .optLabels(labels) // Set the Labels
            .setSampling(batchSize, false) // set the batch size and random sampling to false
            .build();

    // Attach Gradients
    for (NDArray param : params) {
      param.setRequiresGradient(true);
    }

    NDArray trueW = manager.create(W);
    for (int epoch = 0; epoch < numEpochs; epoch++) {
      // Assuming the number of examples can be divided by the batch size, all
      // the examples in the training dataset are used once in one epoch
      // iteration. The features and tags of minibatch examples are given by X
      // and y respectively.
      for (Batch batch : dataset.getData(manager)) {
        NDArray x = batch.getData().head();
        log.info("X = {}", x);
        NDArray y = batch.getLabels().head();
        log.info("y = {}", y);

        try (GradientCollector gc = Engine.getInstance().newGradientCollector()) {
          // Minibatch loss in X and y
          NDArray w = params.get(0);
          log.info("w = {}", w);
          NDArray b = params.get(1);
          log.info("b = {}", b);

          // y calculé = X.w+b
          // y calculé =
          // [[x11,  x12],
          //  [x21,  x22],
          //  ...
          //  [x91,  x92],
          //  [x101, x102],
          // ]
          //        ⊤
          // [[w1],
          //  [w2]
          // ]
          //        +
          // b
          //
          // = [[x11.w1 + x12.w2 + b],
          //    [x21.w1 + x22.w2 + b],
          //       ...
          //    [x91.w1 + x92.w2 + b]
          //    [x101.w1 + x102.w2 + b]
          //   ]
          NDArray yCalcule = linreg(x, w, b);
          log.info("y calculé = {}", yCalcule);
          NDArray losses = squaredLoss(yCalcule, y);
          log.info("Perte : {}", losses);
          gc.backward(losses); // Compute gradient on losses with respect to w and b
        }
        stochasticGradientDescent(
            params, learningRate, batchSize); // Update parameters using their gradient

        batch.close();
      }
      NDArray w = params.get(0);
      NDArray b = params.get(1);
      NDArray trainL = squaredLoss(linreg(features, w, b), labels);
      log.info("epoch {}, loss {}", epoch + 1, trainL.mean().getFloat());
      float[] wCalcule = trueW.sub(w.reshape(trueW.getShape())).toFloatArray();
      log.info("Error in estimating w: [{}, {}]", wCalcule[0], wCalcule[1]);
      log.info("Error in estimating b: {}", B - b.getFloat());
    }
  }

  /**
   * Définition du modèle :
   *
   * <p>x.w+b
   */
  public static NDArray linreg(NDArray x, NDArray w, NDArray b) {
    return x.dot(w).add(b);
  }

  /**
   * Définition de la fonction estimant la perte :
   *
   * <p>1/2*(ycalc − y)^2
   */
  public static NDArray squaredLoss(NDArray yCalcule, NDArray y) {
    return (yCalcule.sub(y.reshape(yCalcule.getShape())))
        .mul((yCalcule.sub(y.reshape(yCalcule.getShape()))))
        .div(2);
  }

  // Generate y = X w + b + noise
  public static DataPoints syntheticData(NDManager manager, NDArray w, float b, int numExamples) {
    NDArray xVector = manager.randomNormal(new Shape(numExamples, w.size()));
    NDArray y = xVector.dot(w).add(b);
    // Add noise
    y = y.add(manager.randomNormal(MEAN, STANDARD_DEVIATION, y.getShape(), DataType.FLOAT32));
    return new DataPoints(xVector, y);
  }

  public static void scatterPlot(float[] x, float[] y) {
    Table data =
        Table.create("Data").addColumns(FloatColumn.create("x", x), FloatColumn.create("y", y));

    Plot.show(ScatterPlot.create("Synthetic Data", data, "X", "y"));
  }

  @Getter
  public static class DataPoints {
    private final NDArray x;
    private final NDArray y;

    public DataPoints(NDArray x, NDArray y) {
      this.x = x;
      this.y = y;
    }
  }
}
