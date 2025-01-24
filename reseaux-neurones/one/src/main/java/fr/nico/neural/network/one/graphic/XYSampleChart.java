package fr.nico.neural.network.one.graphic;

import fr.nico.neural.network.one.application.out.XYGraphicDisplay;
import fr.nico.neural.network.one.application.shared.XYGraphProperties;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.demo.charts.ExampleChart;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.colors.ChartColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class XYSampleChart implements XYGraphicDisplay, ExampleChart<XYChart> {

  private static final Logger LOGGER = LoggerFactory.getLogger(XYSampleChart.class);

  private static final int DPI = 100;

  private final String chartName;
  private final XYGraphProperties graphProperties;

  private XYChart chart;

  XYSampleChart(XYGraphProperties graphProperties) {
    this.graphProperties = graphProperties;
    this.chartName = graphProperties.graphicName();
  }

  @Override
  public void addXYSeries(String serieName, List<Double> xValues, List<Double> yValues) {
    getChart().addSeries(serieName, xValues, yValues);
  }

  @Override
  public void export() {
    Thread.ofPlatform().start(this::saveImageFile);
  }

  @Override
  public void display() {
    Thread.ofPlatform()
        .start(() -> waitUntilFrameIsClosed(new SwingWrapper<>(getChart()).displayChart()));
  }

  @Override
  public XYChart getChart() {
    if (chart == null) {
      initializeChart();
    }
    return chart;
  }

  @Override
  public String getExampleChartName() {
    return chartName;
  }

  private void saveImageFile() {
    try {
      File outputFile = Files.createTempFile(getExampleChartName(), ".jpg").toFile();
      LOGGER.info("Export du graphique vers le fichier : {}", outputFile);
      BitmapEncoder.saveBitmapWithDPI(
          getChart(), outputFile.getAbsolutePath(), BitmapEncoder.BitmapFormat.JPG, DPI);
    } catch (Exception e) {
      LOGGER.error("Problème lors de l'export du graphique", e);
    }
  }

  @SuppressWarnings("java:S2142")
  private static void waitUntilFrameIsClosed(JFrame frame) {
    do {
      try {
        Thread.sleep(1000);
      } catch (Exception e) {
        LOGGER.debug("Problème durant l'attente de la fermeture de la fenêtre du graphique", e);
      }
    } while (frame.isShowing());
  }

  private void initializeChart() {
    this.chart =
        new XYChartBuilder()
            .width(graphProperties.width())
            .height(graphProperties.height())
            .title(getExampleChartName())
            .xAxisTitle(graphProperties.xAxisTitle())
            .yAxisTitle(graphProperties.yAxisTitle())
            .build();

    initChartStyler(this.chart.getStyler());
  }

  private void initChartStyler(XYStyler styler) {
    styler.setPlotBackgroundColor(ChartColor.GREY.getColor());
    styler.setPlotGridLinesColor(Color.WHITE);
    styler.setChartBackgroundColor(Color.WHITE);
    styler.setLegendBackgroundColor(Color.PINK);
    styler.setChartFontColor(Color.MAGENTA);
    styler.setChartTitleBoxBackgroundColor(new Color(0, 222, 0));
    styler.setChartTitleBoxVisible(true);
    styler.setChartTitleBoxBorderColor(Color.BLACK);
    styler.setPlotGridLinesVisible(true);
    styler.setAxisTickPadding(20);
    styler.setAxisTickMarkLength(15);
    styler.setPlotMargin(20);
    styler.setChartTitleVisible(false);
    styler.setChartTitleFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
    styler.setLegendFont(new Font(Font.SERIF, Font.PLAIN, 18));
    styler.setLegendPosition(Styler.LegendPosition.InsideSE);
    styler.setLegendSeriesLineLength(12);
    styler.setAxisTitleFont(new Font(Font.SANS_SERIF, Font.ITALIC, 18));
    styler.setAxisTickLabelsFont(new Font(Font.SERIF, Font.PLAIN, 11));
    styler.setDatePattern("yyyy-MM");
    styler.setDecimalPattern("#0.00");
    styler.setLocale(Locale.getDefault());
  }
}
