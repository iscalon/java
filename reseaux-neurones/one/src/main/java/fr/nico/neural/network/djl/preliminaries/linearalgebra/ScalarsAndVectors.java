package fr.nico.neural.network.djl.preliminaries.linearalgebra;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import java.util.Objects;

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

      // ============ Produit scalaire
      // Etant donnés 2 vecteurs : x et y ∈ Rd, leur produit scalaire x⊤y (ou ⟨x,y⟩) est la somme de
      // tous les produits des éléments à la même position : x⊤y=∑i=1..d(xiyi)
      try (NDArray x = manager.arange(4f);
          NDArray y = manager.ones(new Shape(4), DataType.FLOAT32).add(2f)) {
        // x = [0, 1, 2, 3]
        // y = [3, 3, 3, 3]
        // <x,y> = 0*3 + 1*3 + 2*3 + 3*3 = 18
        // NB: le produit scalaire djl n'est pas possible sur des INT
        assert x.dot(y).contentEquals(18f);
        assert Objects.deepEquals(x.dot(y).toFloatArray(), x.mul(y).sum().toFloatArray());
        // x⃗ ⋅y⃗ =∥x⃗ ∥∥y⃗ ∥cos(α)
        // ∥x⃗ ∥=√(x1^2+x2^2+…+xn^2)
      }
    }
  }
}
