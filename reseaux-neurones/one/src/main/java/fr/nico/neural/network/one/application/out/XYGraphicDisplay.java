package fr.nico.neural.network.one.application.out;

import java.util.List;

public interface XYGraphicDisplay {

  void addXYSeries(String serieName, List<Double> xValues, List<Double> yValues);

  void export();

  void display();
}
