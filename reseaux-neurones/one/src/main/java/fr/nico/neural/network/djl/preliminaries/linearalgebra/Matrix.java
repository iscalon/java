package fr.nico.neural.network.djl.preliminaries.linearalgebra;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import com.google.common.primitives.Booleans;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Matrix {

  private static final int[] ROW_AXIS = {0};
  private static final int[] COL_AXIS = {1};
  private static final boolean KEEP_DIMS = true;

  @SuppressWarnings("java:S3346")
  public static void main(String[] args) {

    // A ∈ Rm×n exprime que la matrice A est constituée de m lignes et n colonnes
    // et ses valeurs appartiennent à l'ensemble des nombres réels
    // aij est la valeur 'a' qui est à la ième ligne, jème colonne
    // aij peut aussi s'écrire : [A]ij

    try (NDManager manager = NDManager.newBaseManager()) {
      int m = 5;
      int n = 4;
      try (NDArray matrixA = manager.arange(m * n).reshape(m, n)) {
        // [[0.,  1.,  2.,  3.],
        // [ 4.,  5.,  6.,  7.],
        // [ 8.,  9., 10., 11.],
        // [12., 13., 14., 15.],
        // [16., 17., 18., 19.],
        // ]
        assert matrixA.get(1).get(2).contentEquals(6);

        // La forme de A ∈ Rm×n est : (m, n) ou m x n
        Shape shape = matrixA.getShape();
        assert shape.get(0) == m;
        assert shape.get(1) == n;

        // La transposée de A est notée A⊤ et si B = A⊤, alors bij=aji pour tous les i et j.
        // Par conséquent la transposée de A est une matrice n×m
        // [[ 0.,  4.,  8., 12., 16.],
        //  [ 1.,  5.,  9., 13., 17.],
        //  [ 2.,  6., 10., 14., 18.],
        //  [ 3.,  7., 11., 15., 19.],
        // ]
        NDArray transposeeA = matrixA.transpose();
        assert transposeeA.get(1).get(2).contentEquals(9);
        shape = transposeeA.getShape();
        assert shape.get(0) == n;
        assert shape.get(1) == m;

        // Si une matrice B est égale à sa transposée alors on dit que B est symétrique
        try (NDArray matrixB = manager.create(new int[][] {{1, 2, 3}, {2, 0, 4}, {3, 4, 5}})) {
          log.info("B : {}", matrixB);
          // [[1., 2., 3.],
          //  [2., 0., 4.],
          //  [3., 4., 5.],
          // ]

          boolean[] equalities = matrixB.eq(matrixB.transpose()).toBooleanArray();
          boolean equals = Booleans.asList(equalities).stream().allMatch(eq -> eq);
          log.info("B.eq(B⊤) : {}", equals);
          assert equals;
        }

        NDArray matrixB = matrixA.duplicate();
        // B ∈ Rm×n et ses éléments sont notés bij.
        log.info("B : {}", matrixB);
        // Le produit matriciel de Hadamard : A ⊙ B est égal à C
        // avec [C]ij = aij*bij
        NDArray matrixC = matrixA.mul(matrixB);
        log.info("A ⊙ B : {}", matrixC);
        // [[  0,   1,   4,   9],
        //  [ 16,  25,  36,  49],
        //  [ 64,  81, 100, 121],
        //  [144, 169, 196, 225],
        //  [256, 289, 324, 361],
        // ]
        assert matrixC.get(2).get(2).contentEquals(100);

        // ============= Réductions
        // La somme des éléments d'une matric A m×n peut s'écrire ∑i=1..m(∑j=1..n(aij))
        assert matrixA.sum().contentEquals(190);

        // On peut calculer la somme de chaque colonne
        // en réduisant le calcul le long de l'axe 0 (ligne)
        // Ca va nous donner 1 vecteur avec n valeurs.
        assert Objects.deepEquals(matrixA.sum(ROW_AXIS).toIntArray(), new int[] {40, 45, 50, 55});

        // On peut calculer la somme de chaque ligne
        // en réduisant le calcul le long de l'axe 1 (colonne)
        // Ca va nous donner 1 vecteur avec m valeurs.
        assert Objects.deepEquals(
            matrixA.sum(COL_AXIS).toIntArray(), new int[] {6, 22, 38, 54, 70});

        // Moyenne des éléments de A
        NDArray matrixAMean = matrixA.mean();
        assert matrixAMean.contentEquals(9.5);
        assert matrixA.sum().div(matrixA.size() /* m x n */).contentEquals(matrixAMean);

        // Moyenne des éléments de A en réduisant sur l'axe 0
        NDArray matrixARowsMean = matrixA.mean(ROW_AXIS);
        assert Objects.deepEquals(matrixARowsMean.toFloatArray(), new float[] {8, 9, 10, 11});
        assert matrixA
            .sum(ROW_AXIS)
            .div(matrixA.getShape().get(0) /* m */)
            .contentEquals(matrixARowsMean);

        // ============= Somme non-réduction

        // Matrice somme sur l'axe 1 => m lignes, 1 colonne
        NDArray matrixAColsSum = matrixA.sum(COL_AXIS, KEEP_DIMS);
        // [[ 6.],
        //  [22.],
        //  [38.],
        //  [54.],
        //  [70.],
        // ]
        assert matrixAColsSum.getShape().get(0) == m;
        assert matrixAColsSum.getShape().get(1) == 1;
        assert Objects.deepEquals(matrixAColsSum.toIntArray(), new int[] {6, 22, 38, 54, 70});

        // Grâce au broadcasting on peut faire A / ∑A
        log.info("A / ∑A (broadcasting) : {}", matrixA.div(matrixAColsSum));
        // [[0.    , 0.1667, 0.3333, 0.5   ],
        //  [0.1818, 0.2273, 0.2727, 0.3182],
        //  [0.2105, 0.2368, 0.2632, 0.2895],
        //  [0.2222, 0.2407, 0.2593, 0.2778],
        //  [0.2286, 0.2429, 0.2571, 0.2714],
        // ]

        // ∑A cumulative sur l'axe 0 (ligne par ligne)
        // Ex : cumSum([[1,2],
        //              [3,4],
        //              [5,6]])
        // On obtient :
        // [[1,2],
        //  [4,6],
        //  [9,12]]
        //
        // Puisque les sommes cumulées sont : 1, 1+3=4, 1+3+5=9 et 2, 2+4=6, 2+4+6=12.
        NDArray matrixARowsCumSum = matrixA.cumSum(0);
        log.info("∑A cumulative sur l'axe 0 : {}", matrixARowsCumSum);
        // [[ 0,  1,  2,  3],
        //  [ 4,  6,  8, 10],
        //  [12, 15, 18, 21],
        //  [24, 28, 32, 36],
        //  [40, 45, 50, 55],
        // ]
        assert matrixA
            .sum(ROW_AXIS)
            .contentEquals(matrixARowsCumSum.get(m - 1 /* dernière ligne */));

        NDArray matrixAColsCumSum = matrixA.cumSum(1);
        log.info("∑A cumulative sur l'axe 1 : {}", matrixAColsCumSum);
        assert matrixA
            .sum(COL_AXIS)
            .contentEquals(
                matrixAColsCumSum
                    .transpose()
                    .get(n - 1 /* dernière ligne de la transposée = dernière colonne de A */));
      }
    }
  }
}
