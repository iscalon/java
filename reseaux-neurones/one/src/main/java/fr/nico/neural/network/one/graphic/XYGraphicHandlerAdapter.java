package fr.nico.neural.network.one.graphic;

import fr.nico.neural.network.one.application.out.XYGraphicDisplay;
import fr.nico.neural.network.one.application.out.XYGraphicHandler;
import fr.nico.neural.network.one.application.shared.XYGraphProperties;

class XYGraphicHandlerAdapter implements XYGraphicHandler {

  @Override
  public XYGraphicDisplay initializeGraphics(XYGraphProperties properties) {
    return new XYSampleChart(properties);
  }
}
