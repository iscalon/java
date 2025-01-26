package fr.nico.neural.network.one.application.out;

import fr.nico.neural.network.one.application.shared.XYSerieProperties;
import java.util.List;

public interface XYGraphicDisplay {

  void addXYSerie(
      String serieName, List<Double> xValues, List<Double> yValues, XYSerieProperties properties);

  void export();

  void display();
}
