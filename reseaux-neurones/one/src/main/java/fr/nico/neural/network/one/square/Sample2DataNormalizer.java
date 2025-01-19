package fr.nico.neural.network.one.square;

import fr.nico.neural.network.one.shared.DataNormalizer;
import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Permet de normaliser les données pour l'entrainement de résaux de neurones sur l'extrapolation de
 * f(x)=x^2
 */
@Component
public class Sample2DataNormalizer implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(Sample2DataNormalizer.class);

  // On veut projeter les valeurs sur l'intervalle [-1; 1]
  private static final double INTERVALLE_DESTINATION_MIN = -1;
  private static final double INTERVALLE_DESTINATION_MAX = 1;

  // On fixe les valeurs inférieures et supérieures que peut prendre X dans l'échantillon de données
  private static final double COLONNE_X_VALEUR_INFERIEURE = 0;
  private static final double COLONNE_X_VALEUR_SUPERIEURE = 5;

  // On fixe les valeurs inférieures et supérieures que peut prendre Y dans l'échantillon de données
  private static final double COLONNE_Y_VALEUR_INFERIEURE = 0;
  private static final double COLONNE_Y_VALEUR_SUPERIEURE = 5;

  private static final DataNormalizer X_NORMALIZER =
      DataNormalizer.toInterval(INTERVALLE_DESTINATION_MIN, INTERVALLE_DESTINATION_MAX)
          .fromInterval(COLONNE_X_VALEUR_INFERIEURE, COLONNE_X_VALEUR_SUPERIEURE);

  private static final DataNormalizer Y_NORMALIZER =
      DataNormalizer.toInterval(INTERVALLE_DESTINATION_MIN, INTERVALLE_DESTINATION_MAX)
          .fromInterval(COLONNE_Y_VALEUR_INFERIEURE, COLONNE_Y_VALEUR_SUPERIEURE);

  // Le nom des fichiers à normaliser
  private static final String RESSOURCES_ENTRAINEMENT_RESEAU = "Sample2_Train_Real.csv";
  private static final String RESSOURCES_TEST_RESEAU = "Sample2_Test_Real.csv";

  public static final CSVFormat CSV_FORMAT =
      CSVFormat.Builder.create()
          .setHeader("    xPoint,Actual value y")
          .setSkipHeaderRecord(true)
          .setDelimiter(',')
          .get();

  private static void normalizeFile(String fileNameToNormalize) {
    try (CSVParser fileToNormalize = getCSVFile(fileNameToNormalize)) {
      List<CSVRecord> records = fileToNormalize.getRecords();
      List<Double> xNormalizedValues = X_NORMALIZER.normalize(readXValuesFrom(records));
      List<Double> yNormalizedValues = Y_NORMALIZER.normalize(readYValuesFrom(records));

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

  @Override
  public void run(ApplicationArguments args) throws Exception {
    LOGGER.info(
        "Démarrage de la normalisation des données sur l'intervalle : [{}; {}]",
        INTERVALLE_DESTINATION_MIN,
        INTERVALLE_DESTINATION_MAX);

    Set.of(RESSOURCES_ENTRAINEMENT_RESEAU, RESSOURCES_TEST_RESEAU)
        .forEach(Sample2DataNormalizer::normalizeFile);
  }
}
