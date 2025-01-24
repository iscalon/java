package fr.nico.neural.network.one.square;

import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Permet de normaliser les données pour l'entrainement de résaux de neurones sur l'extrapolation de
 * f(x)=x^2
 */
@Component
public class Sample2DataNormalizer implements ApplicationRunner {

  // On fixe les valeurs inférieures et supérieures que peut prendre X dans l'échantillon de données
  private static final double COLONNE_X_VALEUR_INFERIEURE = 0;
  private static final double COLONNE_X_VALEUR_SUPERIEURE = 5;

  // On fixe les valeurs inférieures et supérieures que peut prendre Y dans l'échantillon de données
  private static final double COLONNE_Y_VALEUR_INFERIEURE = 0;
  private static final double COLONNE_Y_VALEUR_SUPERIEURE = 5;

  // Le nom des fichiers à normaliser
  private static final String RESSOURCES_ENTRAINEMENT_RESEAU = "Sample2_Train_Real.csv";
  private static final String RESSOURCES_TEST_RESEAU = "Sample2_Test_Real.csv";

  private static final CsvXYFileNormalizer FILE_NORMALIZER =
      new CsvXYFileNormalizer(
          COLONNE_X_VALEUR_INFERIEURE,
          COLONNE_X_VALEUR_SUPERIEURE,
          COLONNE_Y_VALEUR_INFERIEURE,
          COLONNE_Y_VALEUR_SUPERIEURE);

  @Override
  public void run(ApplicationArguments args) {
    Set.of(RESSOURCES_ENTRAINEMENT_RESEAU, RESSOURCES_TEST_RESEAU)
        .forEach(FILE_NORMALIZER::normalizeFile);
  }
}
