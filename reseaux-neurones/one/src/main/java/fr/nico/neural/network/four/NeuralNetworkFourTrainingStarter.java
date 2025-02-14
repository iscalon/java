package fr.nico.neural.network.four;

import static java.util.Objects.requireNonNull;

import fr.nico.neural.network.four.application.NeuralNetworkFourTraining;
import fr.nico.neural.network.one.application.shared.DataSetProperties;
import fr.nico.neural.network.one.application.shared.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class NeuralNetworkFourTrainingStarter implements ApplicationRunner {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(NeuralNetworkFourTrainingStarter.class);

  private final NeuralNetworkFourTraining trainers;

  NeuralNetworkFourTrainingStarter(NeuralNetworkFourTraining trainers) {
    this.trainers = requireNonNull(trainers);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    String[] sourceArgs = args.getSourceArgs();
    if (!"train-network-four".equals(sourceArgs[0])) {
      LOGGER.info("Pas d'entrainement du réseau 4 avec les propriétés : {}", sourceArgs);
      return;
    }
    DataSetProperties properties =
        DataSetProperties.builder()
            .fileName(sourceArgs[1])
            .considerHeader(true)
            .numberOfInput(3)
            .numberOfOutput(5)
            .xInterval(new Interval(1, 60))
            .yInterval(new Interval(0, 1))
            .build();
    LOGGER.info("Entrainement du réseau avec les propriétés : {}", properties);
    this.trainers.trainAndSaveNetworkUsing(properties);
  }
}
