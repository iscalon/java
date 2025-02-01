package fr.nico.neural.network.one.application.in;

import fr.nico.neural.network.one.application.shared.DataNormalizer;
import fr.nico.neural.network.one.application.shared.Interval;
import fr.nico.neural.network.one.application.shared.NetworkProperties;
import java.nio.file.Paths;
import java.util.function.UnaryOperator;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.springframework.stereotype.Component;

@Component
class NeuralNetworkTesting implements NeuralNetworkTestUseCase {

  @Override
  public UnaryOperator<Number> testNetworkUsing(NetworkProperties properties) {
    BasicNetwork network =
        (BasicNetwork)
            EncogDirectoryPersistence.loadObject(Paths.get(properties.filePath()).toFile());

    Interval xInterval = properties.xInterval();
    Interval yInterval = properties.yInterval();
    Interval normalizedInterval = properties.normalizedInterval();

    // Permet de normaliser les valeurs d'entrée X
    DataNormalizer xProjection =
        DataNormalizer.toInterval(normalizedInterval.minValue(), normalizedInterval.maxValue())
            .fromInterval(xInterval.minValue(), xInterval.maxValue());

    // Permet de dénormaliser les valeurs de sortie Y
    DataNormalizer yProjection =
        DataNormalizer.toInterval(yInterval.minValue(), yInterval.maxValue())
            .fromInterval(normalizedInterval.minValue(), normalizedInterval.maxValue());

    return x -> {
      double xNormalized = xProjection.normalize(x).getFirst();
      double yNormalized = network.compute(new BasicMLData(new double[] {xNormalized})).getData(0);
      // On remet le y calculé sur l'intervalle non normalisé
      return yProjection.normalize(yNormalized).getFirst();
    };
  }
}
