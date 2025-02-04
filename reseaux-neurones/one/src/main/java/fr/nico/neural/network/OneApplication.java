package fr.nico.neural.network;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class OneApplication {

  public static void main(String[] args) {
    SpringApplicationBuilder builder = new SpringApplicationBuilder(OneApplication.class);

    // Pour pouvoir afficher les fenêtres graphiques Swing
    builder.headless(false);

    builder.run(args);
  }
}
