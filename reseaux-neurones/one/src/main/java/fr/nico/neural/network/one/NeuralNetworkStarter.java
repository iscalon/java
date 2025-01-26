package fr.nico.neural.network.one;

import static java.util.Objects.requireNonNull;

import fr.nico.neural.network.one.application.in.NeuralNetworkTrainUseCase;
import fr.nico.neural.network.one.application.shared.DataSetProperties;
import fr.nico.neural.network.one.application.shared.Interval;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class NeuralNetworkStarter implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeuralNetworkStarter.class);

  private final NeuralNetworkTrainUseCase trainers;

  NeuralNetworkStarter(NeuralNetworkTrainUseCase trainers) {
    this.trainers = requireNonNull(trainers);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    String[] sourceArgs = args.getSourceArgs();
    if (Stream.of(sourceArgs).noneMatch("train-network"::equals)) {
      LOGGER.info("Pas d'entrainement du réseau");
      return;
    }
    DataSetProperties properties =
        DataSetProperties.builder()
            .fileName(sourceArgs[1])
            .considerHeader(true)
            .numberOfInput(1)
            .numberOfOutput(1)
            .xInterval(new Interval(0, 5))
            .yInterval(new Interval(0, 5))
            .build();
    LOGGER.info("Entrainement du réseau avec les propriétés : {}", properties);
    this.trainers.trainAndSaveNetworkUsing(properties);
  }
}
