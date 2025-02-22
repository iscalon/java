package fr.nico.neural.network.djl.preliminaries.preprocessing;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import fr.nico.neural.network.one.data.normalize.CsvXYFileNormalizer;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import tech.tablesaw.api.BooleanColumn;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.io.csv.CsvReadOptions;

@Slf4j
public class DatasetManipulation {

  private static final String PRIX_COL = "Prix";
  private static final String NOMBRE_DE_PIECES_COL = "Nombre de pièces";
  private static final String ALLEE_COL = "Allée";

  public static final String MISSING_VALUE = "N/A";

  public static Table readCSVData(String fileName, String... missingValueIndicators) {
    return readCSVData(fileName, true, missingValueIndicators);
  }

  public static Table readCSVData(
      String fileName, boolean header, String... missingValueIndicators) {
    return readCSVData(fileName, header, ',', missingValueIndicators);
  }

  public static Table readCSVData(
      String fileName, boolean header, char separator, String... missingValueIndicators) {
    File file = CsvXYFileNormalizer.getFile(fileName);

    String[] missingIndicators =
        Optional.ofNullable(missingValueIndicators).orElseGet(() -> new String[0]);
    missingIndicators =
        Stream.of(ArrayUtils.add(missingIndicators, MISSING_VALUE))
            .distinct()
            .toArray(String[]::new);

    return Table.read()
        .usingOptions(
            CsvReadOptions.builder(file)
                .missingValueIndicator(missingIndicators)
                .header(header)
                .separator(separator)
                .build());
  }

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
    String fileName = "tiny_house.csv";
    Table data = readCSVData(fileName);
    log.info("Données chargées : {}", data);
    /*
         Nombre de pièces  |  Allée  |   Prix   |
        -----------------------------------------
                           |  Pavée  |  127500  |
                        2  |         |  106000  |
                        4  |         |  178100  |
                           |         |  140000  |
    */

    // ======== Gestion des données manquantes

    // ----- Création des inputs
    Table inputs = Table.create(data.columns());
    inputs.removeColumns(PRIX_COL);

    // On va substituer les données manquantes par la moyenne des présentes)
    Column<? super Number> colNombreDePieces =
        (Column<? super Number>) inputs.column(NOMBRE_DE_PIECES_COL);
    int mean = (int) inputs.numberColumn(NOMBRE_DE_PIECES_COL).mean();
    colNombreDePieces.set(colNombreDePieces.isMissing(), mean);

    // ----- Création des outputs
    Table outputs = data.retainColumns(PRIX_COL);

    log.info("Inputs :\n{}", inputs);
    //    Nombre de pièces  |  Allée  |
    //    ------------------------------
    //                   3  |  Pavée  |
    //                   2  |         |
    //                   4  |         |
    //                   3  |         |

    log.info("Outputs :{}", outputs);
    //             Prix   |
    //            ----------
    //            127500  |
    //            106000  |
    //            178100  |
    //            140000  |

    // Comme les allées ont l'air d'être pavées ou non on peut la considérer comme une donnée
    // 'discrète' ou 'catégorique', et donc allée pavée = 1, allée non pavée = 0 par exemple.
    StringColumn alleeCol = inputs.column(ALLEE_COL).asStringColumn();

    // En considérant que la colonne des allées est une catégorie alors on peut
    // obtenir des colonnes de booléens qui sont à true si une donnée est présente, false sinon
    // Donc là comme la seule valeur possible est 'Pavée' on obtiendra 2 colonnes (celle indiquant
    // true/false pour les valeurs = 'Pavée', et sa contraire)
    List<BooleanColumn> dummies = alleeCol.getDummies();

    inputs.removeColumns(ALLEE_COL);
    DoubleColumn alleePaveeCol =
        DoubleColumn.create("Allée pavée", dummies.getFirst().asDoubleArray());
    DoubleColumn alleeNonPaveeCol =
        DoubleColumn.create("Allée non pavée", dummies.get(1).asDoubleArray());

    inputs.addColumns(alleePaveeCol, alleeNonPaveeCol);
    log.info("Inputs (après discrétisation de la colonne {}) :\n{}", ALLEE_COL, inputs);
    //    Nombre de pièces  |  Allée pavée  |  Allée non pavée  |
    //    --------------------------------------------------------
    //                   3  |            1  |                0  |
    //                   2  |            0  |                1  |
    //                   4  |            0  |                1  |
    //                   3  |            0  |                1  |

    // ======== Conversion en NDArray
    try (NDManager ndManager = NDManager.newBaseManager()) {
      NDArray input = ndManager.create(inputs.as().intMatrix());
      NDArray output = ndManager.create(outputs.as().doubleMatrix());
      log.info("Conversion en NDArray\ninput: {}\noutput: {}", input, output);
      //      input: ND: (4, 3) cpu() int32
      //              [[ 3,  1,  0],
      //               [ 2,  0,  1],
      //               [ 4,  0,  1],
      //               [ 3,  0,  1],
      //              ]
      //      output: ND: (4, 1) cpu() float64
      //              [[127500.],
      //               [106000.],
      //               [178100.],
      //               [140000.],
      //              ]
    }
  }
}
