package fr.nico.neural.network.djl.linearneuralnetworks.imageclassificationdataset;

import ai.djl.basicdataset.cv.classification.FashionMnist;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.training.dataset.ArrayDataset;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.dataset.Record;
import ai.djl.translate.TranslateException;
import fr.nico.neural.network.djl.ImageUtils;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MNISTDataset {

  private static final String[] WEAR_LABELS = {
    "t-shirt",
    "trouser",
    "pullover",
    "dress",
    "coat",
    "sandal",
    "shirt",
    "sneaker",
    "bag",
    "ankle boot"
  };

  public static String[] getFashionMnistLabels(int[] labelIndices) {
    String[] convertedLabels = new String[labelIndices.length];
    for (int i = 0; i < labelIndices.length; i++) {
      convertedLabels[i] = getFashionMnistLabel(labelIndices[i]);
    }
    return convertedLabels;
  }

  public static String getFashionMnistLabel(int labelIndice) {
    return WEAR_LABELS[labelIndice];
  }

  public static void main(String[] args) throws TranslateException, IOException {
    System.setProperty("java.awt.headless", "false");
    int batchSize = 256;
    boolean randomShuffle = true;

    FashionMnist mnistTrain =
        FashionMnist.builder()
            .optUsage(Dataset.Usage.TRAIN)
            .setSampling(batchSize, randomShuffle)
            .optLimit(Long.getLong("DATASET_LIMIT", Long.MAX_VALUE))
            .build();

    FashionMnist mnistTest =
        FashionMnist.builder()
            .optUsage(Dataset.Usage.TEST)
            .setSampling(batchSize, randomShuffle)
            .optLimit(Long.getLong("DATASET_LIMIT", Long.MAX_VALUE))
            .build();

    mnistTrain.prepare();
    mnistTest.prepare();

    // 60000
    log.info("Train dataset size : {}", mnistTrain.size());

    // 10000
    log.info("Test dataset size : {}", mnistTest.size());

    try (NDManager manager = NDManager.newBaseManager()) {
      final int scale = 4;
      final int width = 28;
      final int height = 28;
      int numberOfWears = 6;

      showImages(mnistTrain, numberOfWears, width, height, scale, manager);
    }
  }

  public static BufferedImage showImages(
      ArrayDataset dataset, int number, int width, int height, int scale, NDManager manager) {
    // Plot a list of images
    BufferedImage[] images = new BufferedImage[number];
    String[] labels = new String[number];
    for (int i = 0; i < number; i++) {
      Record aRecord = dataset.get(manager, i);
      NDArray array = aRecord.getData().getFirst().squeeze(-1);
      int y = (int) aRecord.getLabels().getFirst().getFloat();
      images[i] = toImage(array, width, height);
      labels[i] = getFashionMnistLabel(y);
    }
    int w = images[0].getWidth() * scale;
    int h = images[0].getHeight() * scale;

    return ImageUtils.showImages(images, labels, w, h);
  }

  private static BufferedImage toImage(NDArray array, int width, int height) {
    System.setProperty("apple.awt.UIElement", "true");
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
    Graphics2D g = (Graphics2D) img.getGraphics();
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        float c = array.getFloat(j, i) / 255; // scale down to between 0 and 1
        g.setColor(new Color(c, c, c)); // set as a gray color
        g.fillRect(i, j, 1, 1);
      }
    }
    g.dispose();
    return img;
  }
}
