package com.nico.wordz.database.adapters;

import com.nico.wordz.domain.Player;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tooling.JpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JpaTest
class PlayerRepositoryAdapterTest {

  @Inject
  private PlayerRepositoryAdapter players;

  @Test
  @DisplayName("create player works")
  void test01() {
    Player player = players.create("John");

    assertThat(player).isEqualTo(new Player("John"));
  }

  @Test
  @DisplayName("find player by name works")
  void test02() {
    Player johnPlayer = players.create("John");

    Optional<Player> foundPlayer = players.findByName("John");

    assertThat(foundPlayer)
            .hasValue(johnPlayer);
  }

  @Test
  @DisplayName("can't find unexisting player")
  void test03() {
    Optional<Player> foundPlayer = players.findByName("JohnDoe");

    assertThat(foundPlayer)
            .isEmpty();
  }
}
