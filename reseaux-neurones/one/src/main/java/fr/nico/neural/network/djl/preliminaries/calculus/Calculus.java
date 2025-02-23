package fr.nico.neural.network.djl.preliminaries.calculus;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import java.util.function.DoubleUnaryOperator;
import lombok.extern.slf4j.Slf4j;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.ScatterTrace;

@Slf4j
public class Calculus {

  public static void main(String[] args) {
    DoubleUnaryOperator f = x -> (3 * Math.pow(x, 2) - 4 * x);
    DoubleUnaryOperator df = x -> 6 * x - 4;

    try (NDManager manager = NDManager.newBaseManager()) {
      NDArray vector = manager.arange(0f, 3f, 0.1f, DataType.FLOAT64);
      double[] x = vector.toDoubleArray();

      double[] fx = new double[x.length];
      for (int i = 0; i < x.length; i++) {
        fx[i] = f.applyAsDouble(x[i]);
      }

      double[] dfx = new double[x.length];
      for (int i = 0; i < x.length; i++) {
        dfx[i] = df.applyAsDouble(x[i]);
      }

      double[] tangenteXegal1 = new double[x.length];
      double x0 = 1;
      for (int i = 0; i < x.length; i++) {
        tangenteXegal1[i] = df.applyAsDouble(x0) * (x[i] - x0) + f.applyAsDouble(x0);
      }

      Figure figure =
          plotLineAndSegment(
              x,
              fx,
              dfx,
              tangenteXegal1,
              "f(x)",
              "f'(x)",
              "Tangent line(x=1)",
              "x",
              "f(x)",
              1024,
              780);
      Plot.show(figure);
    }
  }

  private static Figure plotLineAndSegment(
      double[] x,
      double[] y,
      double[] dy,
      double[] segment,
      String trace1Name,
      String trace3Name,
      String trace2Name,
      String xLabel,
      String yLabel,
      int width,
      int height) {
    ScatterTrace trace =
        ScatterTrace.builder(x, y).mode(ScatterTrace.Mode.LINE).name(trace1Name).build();

    ScatterTrace trace2 =
        ScatterTrace.builder(x, segment).mode(ScatterTrace.Mode.LINE).name(trace2Name).build();

    ScatterTrace trace3 =
        ScatterTrace.builder(x, dy).mode(ScatterTrace.Mode.LINE).name(trace3Name).build();

    Layout layout =
        Layout.builder()
            .height(height)
            .width(width)
            .showLegend(true)
            .xAxis(Axis.builder().title(xLabel).build())
            .yAxis(Axis.builder().title(yLabel).build())
            .build();

    return new Figure(layout, trace, trace2, trace3);
  }
}
