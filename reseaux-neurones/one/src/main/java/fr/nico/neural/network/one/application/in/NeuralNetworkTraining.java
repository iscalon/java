package fr.nico.neural.network.one.application.in;

import fr.nico.neural.network.one.application.out.XYGraphicDisplay;
import fr.nico.neural.network.one.application.out.XYGraphicHandler;
import fr.nico.neural.network.one.application.shared.DataSetProperties;
import fr.nico.neural.network.one.application.shared.XYGraphProperties;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
class NeuralNetworkTraining implements NeuralNetworkTrainUseCase {

  private final XYGraphicHandler graphicHandlers;

  NeuralNetworkTraining(XYGraphicHandler graphicHandlers) {
    this.graphicHandlers = requireNonNull(graphicHandlers);
  }

  @Override
  public void trainAndSaveNetworkUsing(DataSetProperties dataSetProperties) {
    XYGraphicDisplay graphics =
        graphicHandlers.initializeGraphics(
            XYGraphProperties.builder()
                .graphicName("Training")
                .width(800)
                .height(600)
                .xAxisTitle("X")
                .yAxisTitle("Y")
                .build());
    graphics.display();
    graphics.export();
  }
}
