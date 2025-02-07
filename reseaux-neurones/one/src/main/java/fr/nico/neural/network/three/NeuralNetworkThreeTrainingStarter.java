package fr.nico.neural.network.three;

import static java.util.Objects.requireNonNull;

import fr.nico.neural.network.one.application.shared.DataSetProperties;
import fr.nico.neural.network.one.application.shared.Interval;
import fr.nico.neural.network.three.application.NeuralNetworkThreeTraining;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class NeuralNetworkThreeTrainingStarter implements ApplicationRunner {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(NeuralNetworkThreeTrainingStarter.class);

  private final NeuralNetworkThreeTraining trainers;

  NeuralNetworkThreeTrainingStarter(NeuralNetworkThreeTraining trainers) {
    this.trainers = requireNonNull(trainers);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    String[] sourceArgs = args.getSourceArgs();
    if (!"train-network-three".equals(sourceArgs[0])) {
      LOGGER.info("Pas d'entrainement du réseau 3 avec les propriétés : {}", sourceArgs);
      return;
    }
    DataSetProperties properties =
        DataSetProperties.builder()
            .fileName(sourceArgs[1])
            .considerHeader(false)
            .numberOfInput(4)
            .numberOfOutput(1)
            .xInterval(new Interval(-100, 100))
            .yInterval(new Interval(-100, 100))
            .build();
    LOGGER.info("Entrainement du réseau avec les propriétés : {}", properties);
    this.trainers.trainAndSaveNetworkUsing(properties);
  }
}
