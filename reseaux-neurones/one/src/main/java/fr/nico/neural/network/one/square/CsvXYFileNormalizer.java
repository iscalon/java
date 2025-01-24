package fr.nico.neural.network.one.square;

import fr.nico.neural.network.one.shared.DataNormalizer;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class CsvXYFileNormalizer {

  private static final Logger LOGGER = LoggerFactory.getLogger(CsvXYFileNormalizer.class);

  // On veut projeter les valeurs sur l'intervalle [-1; 1]
  private static final double INTERVALLE_DESTINATION_MIN = -1;
  private static final double INTERVALLE_DESTINATION_MAX = 1;

  private final DataNormalizer xNormalizer;

  private final DataNormalizer yNormalizer;

  private static final CSVFormat CSV_FORMAT =
      CSVFormat.Builder.create()
          .setHeader("    xPoint,Actual value y")
          .setSkipHeaderRecord(true)
          .setDelimiter(',')
          .get();

  public CsvXYFileNormalizer() {
    this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
  }

  public CsvXYFileNormalizer(
      double minXValue, double maxXValue, double minYValue, double maxYValue) {
    xNormalizer =
        DataNormalizer.toInterval(INTERVALLE_DESTINATION_MIN, INTERVALLE_DESTINATION_MAX)
            .fromInterval(minXValue, maxXValue);

    yNormalizer =
        DataNormalizer.toInterval(INTERVALLE_DESTINATION_MIN, INTERVALLE_DESTINATION_MAX)
            .fromInterval(minYValue, maxYValue);
  }

  public void normalizeFile(String fileNameToNormalize) {
    LOGGER.info(
        "Démarrage de la normalisation des données du fichier : {} sur l'intervalle : [{}; {}]",
        fileNameToNormalize,
        INTERVALLE_DESTINATION_MIN,
        INTERVALLE_DESTINATION_MAX);
    try (CSVParser fileToNormalize = getCSVFile(fileNameToNormalize)) {
      List<CSVRecord> records = fileToNormalize.getRecords();
      List<Double> xNormalizedValues = xNormalizer.normalize(readXValuesFrom(records));
      List<Double> yNormalizedValues = yNormalizer.normalize(readYValuesFrom(records));

      writeOutputFile(fileNameToNormalize, xNormalizedValues, yNormalizedValues);
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Problème lors de la normalisation des données du fichier " + fileNameToNormalize, e);
    }
  }

  private static void writeOutputFile(
      String fileNameToNormalize, List<Double> xNormalizedValues, List<Double> yNormalizedValues)
      throws IOException {
    String prefix = FilenameUtils.getBaseName(fileNameToNormalize) + "_Norm";
    String suffix = "." + FilenameUtils.getExtension(fileNameToNormalize);
    File outputFile = Files.createTempFile(prefix, suffix).toFile();

    try (final CSVPrinter printer = new CSVPrinter(new FileWriter(outputFile), CSV_FORMAT)) {
      for (int i = 0; i < xNormalizedValues.size(); i++) {
        printer.printRecord(xNormalizedValues.get(i), yNormalizedValues.get(i));
      }
    } finally {
      LOGGER.info("Fichier de sortie normalisé : {}", outputFile);
    }
  }

  private static List<Double> readXValuesFrom(List<CSVRecord> records) {
    return records.stream().map(r -> r.get(0)).map(Double::parseDouble).toList();
  }

  private static List<Double> readYValuesFrom(List<CSVRecord> records) {
    return records.stream().map(r -> r.get(1)).map(Double::parseDouble).toList();
  }

  private static CSVParser getCSVFile(String fileNameToNormalize) throws IOException {
    File file =
        new ClassPathResource(
                "input/" + fileNameToNormalize, Sample2DataNormalizer.class.getClassLoader())
            .getFile();
    return CSV_FORMAT.parse(new FileReader(file));
  }
}
