package fr.nico.neural.network.two.data.window;

import static fr.nico.neural.network.one.data.normalize.CsvXYFileNormalizer.getFile;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowDataset {

  private static final Logger LOGGER = LoggerFactory.getLogger(WindowDataset.class);

  private static final int WINDOW_SIZE = 6;
  private static final int NORMALIZED_DATA_INDEX = 2;

  private WindowDataset() {
    // just to hide it
  }

  private static final CSVFormat CSV_FORMAT =
      CSVFormat.Builder.create()
          .setHeader("1 € en yens;DELTA [-30,30];DELTA [-1,1]")
          .setSkipHeaderRecord(true)
          .setDelimiter(';')
          .get();

  public static void createDatasetFileFrom(String inputFile) {
    LOGGER.info(
        "Démarrage de la création des fenêtres servant de dataset des données du fichier : {}",
        inputFile);
    try (CSVParser file = getCSVFile(inputFile)) {
      List<CSVRecord> records = file.getRecords();
      writeOutputFile(inputFile, creerFenetres(records));
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "Problème lors de la création du dataset à partir du fichier " + inputFile, e);
    }
  }

  private static void writeOutputFile(
      String inputFileName, Collection<? extends List<String>> values) throws IOException {
    String prefix = FilenameUtils.getBaseName(inputFileName) + "_Windowed";
    String suffix = "." + FilenameUtils.getExtension(inputFileName);
    File outputFile = Files.createTempFile(prefix, suffix).toFile();

    try (final CSVPrinter printer = new CSVPrinter(new FileWriter(outputFile), CSV_FORMAT)) {
      printer.printRecords(values.stream());
    } finally {
      LOGGER.info("Fichier de sortie créé : {}", outputFile);
    }
  }

  /**
   * Récupère un parseur de fichier CSV sur le fichier ayant le nom passé en paramètre situé dans le
   * dossier : {@code resources/input}
   *
   * @param fileName
   * @return
   * @throws IOException
   */
  private static CSVParser getCSVFile(String fileName) throws IOException {
    File file = getFile(fileName);
    return CSV_FORMAT.parse(new FileReader(file));
  }

  private static Collection<? extends List<String>> creerFenetres(List<CSVRecord> records) {
    int iterationCount = records.size() - WINDOW_SIZE;
    List<List<String>> result = new ArrayList<>();
    for (int iteration = 0; iteration < iterationCount; iteration++) {
      List<String> inputsAndOutput =
          records.subList(iteration, iteration + WINDOW_SIZE + 1).stream()
              .map(r -> r.get(NORMALIZED_DATA_INDEX))
              .toList();
      result.add(inputsAndOutput);
    }

    return result;
  }

  public static void main(String[] args) {
    WindowDataset.createDatasetFileFrom("TauxMoyens_06-1998_01-2025.csv");
  }
}
