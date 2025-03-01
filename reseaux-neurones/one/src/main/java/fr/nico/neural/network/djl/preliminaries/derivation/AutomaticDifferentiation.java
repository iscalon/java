package fr.nico.neural.network.djl.preliminaries.derivation;

import ai.djl.engine.Engine;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.training.GradientCollector;
import com.google.common.primitives.Booleans;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutomaticDifferentiation {

  public static void main(String[] args) {
    try (NDManager manager = NDManager.newBaseManager()) {
      NDArray x = manager.arange(4f);
      // Si y=2x.x (NB : '.' = produit scalaire)
      // alors ∇y=∇(2x.x)=2∇(x.x)=2*2x=4x=2∇∥x∥^2  (NB: ∇(x.x)=2x)

      // Allocation de mémoire pour le calcul du gradient de y par rapport à x
      x.setRequiresGradient(true);
      try (GradientCollector gc = Engine.getInstance().newGradientCollector()) {
        NDArray y = x.dot(x).mul(2); // y est un scalaire
        log.info("y = {}", y);
        // x = [0, 1, 2, 3]
        // y = 28

        // calcul du gradient
        gc.backward(y);
      } // Fin du gradient collector
      // y = 2x.x = 2*x1^2 + 2*x2^2 + 2*x3^2 + 2*x4^2
      // donc y' = 2*2*x1 + 2*2*x2 + 2*2*x3 + 2*2*x4
      // ∇y = [0, 4, 8, 12] = 4x
      log.info("y = 2x.x = 2*x1^2 + 2*x2^2 + 2*x3^2 + 2*x4^2");
      log.info("∇y = {}", x.getGradient());

      // Calcul du gradient d'une nouvelle fonction y = ∑x = 1*x1 + 1*x2 + 1*x3 + 1*x4
      // donc y' = 1 + 1 + 1 + 1
      // Normalement le gradient de cette fonction sera donc [1, 1, 1, 1]
      try (GradientCollector gc = Engine.getInstance().newGradientCollector()) {
        NDArray y = x.sum();
        gc.backward(y);
      }
      log.info("y = ∑x = 1*x1 + 1*x2 + 1*x3 + 1*x4");
      log.info("∇y = {}", x.getGradient());
      assert x.getGradient().contentEquals(1);

      // ============== Calcul de gradient pour des variables non scalaires

      // Quand on utilise `backward` sur une variable `y`(fonction de `x`) qui est un vecteur,
      // une nouvelle variable scalaire est créée en sommant les éléments de `y`.
      // Et alors le gradient par rapport à x de cette variable scalaire est calculé.
      try (GradientCollector gc = Engine.getInstance().newGradientCollector()) {
        NDArray y = x.mul(x); // y est un vecteur ici
        gc.backward(y);
      }
      // ∇y = [0, 2, 4, 6]
      log.info("x = {}", x);
      log.info("y = x⊙x = [x1^2, x2^2, x3^2, x4^2]");
      log.info("∇y = {}", x.getGradient());

      // ============= Détachement de variable
      try (GradientCollector gc = Engine.getInstance().newGradientCollector()) {
        // y = x⊙x = [x1^2, x2^2, x3^2, x4^2]
        NDArray y = x.mul(x);
        // On va dire que u = y mais que x pour u est considéré comme une constante
        NDArray u = y.stopGradient(); // on oublie que y est dépendant de x
        // z = u ("constante" donc) ⊙ x
        NDArray z = u.mul(x);
        // ∇z = ∇(u * x) = u * ∇x = u * 1 = u
        gc.backward(z);
        // validons que z' = u
        assert Booleans.asList(x.getGradient().eq(u).toBooleanArray()).stream()
            .allMatch(vrai -> vrai);
      }
      try (GradientCollector gc = Engine.getInstance().newGradientCollector()) {
        NDArray y = x.mul(x);
        y = x.mul(x);
        // y = x⊙x = [x1^2, x2^2, x3^2, x4^2]
        // ∇y = [2x1, 2x2, 2x3, 2x4] = 2 * [x1, x2, x3, x4] = 2x
        gc.backward(y);
        // vérifions que ∇y = 2x
        assert Booleans.asList(x.getGradient().eq(x.mul(2)).toBooleanArray()).stream()
            .allMatch(vrai -> vrai);
      }
    } // fin manager
  }
}
