package fr.nico.neural.network.one;

import static java.util.Objects.requireNonNull;

import fr.nico.neural.network.one.application.in.NeuralNetworkTestUseCase;
import fr.nico.neural.network.one.application.shared.Interval;
import fr.nico.neural.network.one.application.shared.NetworkProperties;
import java.util.Arrays;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class NeuralNetworkTestingStarter implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(NeuralNetworkTestingStarter.class);

  private final NeuralNetworkTestUseCase testers;

  NeuralNetworkTestingStarter(NeuralNetworkTestUseCase testers) {
    this.testers = requireNonNull(testers);
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    String[] sourceArgs = args.getSourceArgs();
    if (Arrays.asList(sourceArgs).contains("train-network")) {
      LOGGER.info("Pas de test du réseau, il est en cours d'entrainement");
      return;
    }
    NetworkProperties properties =
        NetworkProperties.builder()
            .filePath(sourceArgs[1])
            .xInterval(new Interval(0, 5))
            .yInterval(new Interval(0, 5))
            .build();
    LOGGER.info("Test du réseau : {}", properties.filePath());
    UnaryOperator<Number> expectedNumberComputer = this.testers.testNetworkUsing(properties);

    log(expectedNumberComputer, 0); // ~0
    log(expectedNumberComputer, 1); // ~1
    log(expectedNumberComputer, 1.414213); // ~2
    log(expectedNumberComputer, 2); // ~4
  }

  private static void log(UnaryOperator<Number> expectedNumberComputer, double x) {
    LOGGER.info("X = {} -> Y = {}", x, expectedNumberComputer.apply(x));
  }
}
