package fr.nico.neural.network.two;

import static java.util.Objects.requireNonNull;

import fr.nico.neural.network.one.application.shared.DataSetProperties;
import fr.nico.neural.network.one.application.shared.Interval;
import fr.nico.neural.network.two.application.NeuralNetworkDiffTraining;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class NeuralNetworkTrainingDiffStarter implements ApplicationRunner {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(NeuralNetworkTrainingDiffStarter.class);

  private final NeuralNetworkDiffTraining trainers;

  NeuralNetworkTrainingDiffStarter(NeuralNetworkDiffTraining trainers) {
    this.trainers = requireNonNull(trainers);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    String[] sourceArgs = args.getSourceArgs();
    if (!"train-network-two".equals(sourceArgs[0])) {
      LOGGER.info("Pas d'entrainement du réseau 2 avec les propriétés : {}", sourceArgs);
      return;
    }
    DataSetProperties properties =
        DataSetProperties.builder()
            .fileName(sourceArgs[1])
            .considerHeader(false)
            .numberOfInput(6)
            .numberOfOutput(1)
            .xInterval(new Interval(-30, 30))
            .yInterval(new Interval(-30, 30))
            .build();
    LOGGER.info("Entrainement du réseau avec les propriétés : {}", properties);
    this.trainers.trainAndSaveNetworkUsing(properties);
  }
}
