package fr.nico.neural.network.djl.preliminaries.linearalgebra;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;

public class ScalarsAndVectors {

  @SuppressWarnings("java:S3346")
  public static void main(String[] args) {
    try (NDManager manager = NDManager.newBaseManager()) {

      // ============ Scalaires
      try (NDArray x = manager.create(3f);
          NDArray y = manager.create(2f)) {
        assert x.add(y).contentEquals(5f);
        assert x.mul(y).contentEquals(6f);
        assert x.div(y).contentEquals(1.5f);
        assert x.pow(y).contentEquals(9f);
      }

      // ============ Vecteurs

      // Ils sont écrits en colonne en mathématiques
      //       _____
      //      | x1 |
      //      | x2 |
      // X =  |... |
      //      | xn |
      //      -----

      // [0, 1, 2, 3]
      NDArray vector = manager.arange(4);
      long vectorDimension = vector.size(0 /* index de l'axe : 0 comme le vecteur n'a qu'un axe */);
      assert vectorDimension == 4;

      // On peut obtenir la dimension grâce à 'shape' qui retourne les dimensions pour chaque axe
      // d'un NDArray (ATTENTION : dimension - le nombre d'élément d'un axe - est confusant avec
      // dimension - le nombre d'axes -)
      assert vector.getShape().get(0) == 4;

      // TODO : Produit scalaire de vecteurs
    }
  }
}
