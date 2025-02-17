package fr.nico.neural.network.djl.preliminaries.datamanipulation;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.index.NDIndex;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataManipulationDemo {

  /** Le NDManager est un Autocloseable il faudrait l'encapsuler dans un "try with resources" */
  private static final NDManager ND_MANAGER = NDManager.newBaseManager();

  public static void main(String[] args) {
    NDArray vector = ND_MANAGER.arange(12);
    /*
    [ 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11]
     */
    log.info("Ici le NDArray est un vecteur allant de 0 à 11 : {}", vector);
    log.info(
        "La taille du NDArray sur la 1ère dimension est 12 : {}", vector.getShape().get(0) == 12);
    log.info(
        "La taille totale du NDArray (le produit de tous les éléments de chaque dimension) est 12 : {}",
        vector.size() == 12);

    NDArray matrix = vector.reshape(3, 4);
    /*
    [[ 0,  1,  2,  3],
     [ 4,  5,  6,  7],
     [ 8,  9, 10, 11],
    ]
    */
    log.info("Le Vector(12) transformé en une matrice de 3 lignes et 4 colonnes : {}", matrix);

    matrix = vector.reshape(-1, 4);
    log.info(
        "Le Vector(12) transformé en une matrice de 3 lignes et 4 colonnes (sans préciser le nombre de lignes) : {}",
        matrix);

    matrix = vector.reshape(3, -1);
    log.info(
        "Le Vector(12) transformé en une matrice de 3 lignes et 4 colonnes (sans préciser le nombre de colonnes) : {}",
        matrix);

    // Attention si on demande au manager de créer une matrice en passant seulement une Shape
    // alors il prend une partie existante en mémoire et nous donne la main dessus
    // mais elle peut contenir n'importe quelle valeur.
    // Donc si nous voulons une matrice de 0 il faut utliser la méthode 'zeros', de 1 il faut
    // utiliser 'ones'.
    NDArray two3x4MatrixOfZeros = ND_MANAGER.zeros(new Shape(2, 3, 4));
    log.info(
        "NDArray de 2 matrices 3 x 4 (tensor 2 x 3 x 4) contenant seulement des 0 : {}",
        two3x4MatrixOfZeros);

    NDArray normalDistributionMatrix =
        ND_MANAGER.randomNormal(0f, 0.5f, new Shape(3, 4), DataType.FLOAT32);
    /*
    [[ 0.5815,  1.1061,  0.2419,  0.387 ],
     [ 0.1498,  0.5217,  0.0765,  0.592 ],
     [-0.5844,  0.9459,  0.779 , -0.6174],
    ]
    */
    log.info(
        "Matrice 3 x 4 dont les éléments sont randomisés en suivant une distribution gaussienne normale de moyenne 0 et d'écart type 0.5 : {}",
        normalDistributionMatrix);

    NDArray customMatrix =
        ND_MANAGER.create(new float[] {2, 1, 4, 3, 1, 2, 3, 4, 4, 3, 2, 1}, new Shape(3, 4));
    log.info(
        "Définition d'une matrice 3 x 4 avec les valeurs fournies [2, 1, 4, 3, 1, 2, 3, 4, 4, 3, 2, 1] : {}",
        customMatrix);

    // ========    Opérations élément par élément
    NDArray xVector = ND_MANAGER.create(new float[] {1f, 2f, 4f, 8f});
    NDArray yVector = ND_MANAGER.create(new float[] {2f, 2f, 2f, 2f});

    log.info(
        "(1, 2, 4, 8) + (2, 2, 2, 2) = (3, 4, 6, 10) : {}",
        xVector.add(yVector).equals(ND_MANAGER.create(new float[] {3f, 4f, 6f, 10f})));

    log.info(
        "(1, 2, 4, 8) - (2, 2, 2, 2) = (-1, 0, 2, 6) : {}",
        xVector.sub(yVector).equals(ND_MANAGER.create(new float[] {-1f, 0f, 2f, 6f})));

    log.info(
        "(1, 2, 4, 8) pow (2, 2, 2, 2) = (1, 4, 16, 64) : {}",
        xVector.pow(yVector).equals(ND_MANAGER.create(new float[] {1f, 4f, 16f, 64f})));

    // ========    Opérations d'algèbre linéaire
    /*
    [1., 2., 4., 8.] concat [2., 2., 2., 2.] -> [1., 2., 4., 8., 2., 2., 2., 2.]
     */
    log.info(
        "{} concaténé avec {} sur l'axe 0 : {}",
        xVector,
        yVector,
        xVector.concat(yVector) /* si pas précisé on concatène selon l'axe 0 */);

    /*
    [[2., 1., 4., 3.],
     [1., 2., 3., 4.],
     [4., 3., 2., 1.],
    ]
        concat selon axe 0
    [1., 2., 4., 8.]
    =
    [[2., 1., 4., 3.],
     [1., 2., 3., 4.],
     [4., 3., 2., 1.],
     [1., 2., 4., 8.],
    ]
    */
    log.info(
        "{} concaténé avec {} sur l'axe 0 : {}",
        customMatrix,
        xVector,
        customMatrix.concat(xVector.reshape(1, 4)));

    NDArray floatMatrix = ND_MANAGER.arange(12f).reshape(3, 4);
    /*
    [[2., 1., 4., 3.],
     [1., 2., 3., 4.],
     [4., 3., 2., 1.],
    ]
     concat selon axe 1
    [[ 0.,  1.,  2.,  3.],
     [ 4.,  5.,  6.,  7.],
     [ 8.,  9., 10., 11.],
    ]
     =
    [[ 2.,  1.,  4.,  3.,  0.,  1.,  2.,  3.],
     [ 1.,  2.,  3.,  4.,  4.,  5.,  6.,  7.],
     [ 4.,  3.,  2.,  1.,  8.,  9., 10., 11.],
    ]
    */
    log.info(
        "{} concaténé avec {} sur l'axe 1 : {}",
        customMatrix,
        floatMatrix,
        customMatrix.concat(floatMatrix, 1));

    NDArray sum = matrix.sum();
    log.info("Somme de tous les éléments de {} = 66 : {}", matrix, sum.getInt() == 66);

    // ===========    Broadcasting

    // Premièrement, étendre l'un des tableaux ou les deux en copiant des éléments de manière
    // appropriée, de sorte qu'après cette transformation, les deux tableaux aient la même forme.
    // Deuxièmement, effectuer les opérations par éléments sur les tableaux résultants.
    //
    // Dans la plupart des cas, nous diffusons le long d'un axe où un tableau n'a initialement
    // qu'une longueur de 1.

    NDArray a = ND_MANAGER.arange(3f).reshape(3, 1);
    NDArray b = ND_MANAGER.arange(2f).reshape(1, 2);
    // Comme a et b sont respectivement des matrices 3×1 et 1×2, leurs formes ne correspondent pas
    // si nous voulons les additionner. Nous diffusons les entrées des deux matrices dans une
    // matrice 3×2 plus grande de la manière suivante : pour la matrice a, nous répliquons les
    // colonnes et pour la matrice b, nous répliquons les lignes avant d'additionner les deux
    // éléments.
    /*
    [[0.],
     [1.],
     [2.],
    ]
    +
    [[0., 1.],
    ]
    =
    [[0., 0.],      [[0., 1.],
     [1., 1.],  +    [0., 1.],
     [2., 2.],       [0., 1.]
    ]               ]
    =
    [[0., 1.],
     [1., 2.],
     [2., 3.],
    ]
    */
    log.info("{} + {} = {}", a, b, a.add(b));

    // ========= Indexing and Slicing
    NDArray lastMatrixRow = floatMatrix.get("-1");
    log.info("La dernière ligne de {} est : {}", floatMatrix, lastMatrixRow);
    NDArray allMatrixRowExceptLast =
        floatMatrix.get(":-1"); // [; -1[ -> les lignes d'index 0 à 1 (car -1 <=> 2 et est exclu)
    log.info(
        "Les 1ères lignes de (en excluant la dernière) {} sont : {}",
        floatMatrix,
        allMatrixRowExceptLast);
    NDArray lastMatrix2Rows = floatMatrix.get("1:3"); // [1; 3[ -> les lignes d'index 1 à 2
    log.info("Les lignes n°2 à n°3 de {} sont : {}", floatMatrix, lastMatrix2Rows);

    NDArray copy = ND_MANAGER.zeros(floatMatrix.getShape(), floatMatrix.getDataType());
    floatMatrix.copyTo(copy);
    copy.set(
        new NDIndex("1, 2"),
        9); // On va mettre la valeur '9' dans la matrice à la 2ème ligne et 3ème colonne
    log.info("Mettons 9 à la 2ème ligne et 3ème colonne de {} : {}", floatMatrix, copy);

    copy = ND_MANAGER.zeros(floatMatrix.getShape(), floatMatrix.getDataType());
    floatMatrix.copyTo(copy);
    copy.set(
        new NDIndex("0:2, :"), // [0, 2[ -> [0, 1]
        12); // On va mettre la valeur '12' dans la matrice dans toutes les colonnes de la 1ère et
    // 2ème ligne
    log.info(
        "Mettons 12 dans toutes les colonnes de la 1ère à la 2ème ligne de {} : {}",
        floatMatrix,
        copy);

    // ============ Mutating operations (pour économiser de la mémoire)
    NDArray original = ND_MANAGER.zeros(yVector.getShape(), yVector.getDataType());
    yVector.copyTo(original);
    NDArray actual = original.addi(xVector);
    assert actual == original;
    log.info("{} + {} = {}", yVector, xVector, original);
  }
}
