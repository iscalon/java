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

  private static final CSVFormat CSV_FORMAT =
      CSVFormat.Builder.create().setHeader("-").setSkipHeaderRecord(true).setDelimiter(';').get();

  private final int windowSize;
  private final int dataIndex;

  private WindowDataset() {
    this(6, 2);
  }

  private WindowDataset(int windowSize, int dataIndex) {
    this.windowSize = windowSize;
    this.dataIndex = dataIndex;
  }

  public static WindowDataset of(int windowSize, int dataIndex) {
    return new WindowDataset(windowSize, dataIndex);
  }

  public void createDatasetFileFrom(String inputFile) {
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

  public Collection<? extends List<String>> creerFenetres(List<CSVRecord> records) {
    int iterationCount = records.size() - windowSize;
    List<List<String>> result = new ArrayList<>();
    for (int iteration = 0; iteration < iterationCount; iteration++) {
      List<String> inputsAndOutput =
          records.subList(iteration, iteration + windowSize + 1).stream()
              .map(r -> r.get(dataIndex))
              .toList();
      result.add(inputsAndOutput);
    }

    return result;
  }

  public static void main(String[] args) {
    WindowDataset.of(6, 2).createDatasetFileFrom("TauxMoyens_06-1998_01-2025.csv");
    WindowDataset.of(4, 1).createDatasetFileFrom("Prix_Maisons_Appartements_2011-2024.csv");
    WindowDataset.of(4, 2).createDatasetFileFrom("Prix_Maisons_Appartements_2011-2024.csv");
  }
}
