package com.nico.wordz.database.adapters;

import com.nico.wordz.domain.Game;
import com.nico.wordz.domain.Player;
import com.nico.wordz.domain.Word;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tooling.JpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@JpaTest
class GameRepositoryAdapterTest {

    @Inject
    private GameRepositoryAdapter games;
    @Inject
    private PlayerRepositoryAdapter players;

    @Test
    @DisplayName("game creation should work")
    void create() {
        Player player = players.create("John");
        Word word = new Word("PIZZA");
        Game game = Game.create(word, player);

        assertThatNoException()
                .isThrownBy(() -> games.create(game));
    }

    @Test
    @DisplayName("find game by an existing player's name should work")
    void test21() {
        Player player = players.create("John");
        Word word = new Word("PIZZA");
        Game game = Game.create(word, player);
        games.create(game);

        Optional<Game> foundGame = games.findByPlayer(player);

        assertThat(foundGame)
                .hasValue(game);
    }

    @Test
    @DisplayName("find game by an unexisting player shouldn't work")
    void test22() {
        Player player = players.create("John");
        Word word = new Word("PIZZA");
        Game game = Game.create(word, player);
        games.create(game);

        Player unexistingPlayer = new Player("JonhDoe");
        Optional<Game> foundGame = games.findByPlayer(unexistingPlayer);

        assertThat(foundGame)
                .isEmpty();
    }

    @Test
    @DisplayName("game update should work")
    void test03() {
        Player player = players.create("John");
        Word word = new Word("PIZZA");
        Game game = Game.create(word, player);
        games.create(game);
        game = game.withAttempt(new Word("LIZZA"));

        games.update(game);

        Game foundGame = games.findByPlayer(player).orElseThrow();
        assertThat(foundGame.isGameOver())
                .isEqualTo(game.isGameOver());
        assertThat(foundGame.attemptNumber())
                .isEqualTo(game.attemptNumber());
        assertThat(foundGame.player())
                .isEqualTo(game.player());
        assertThat(foundGame.word())
                .isEqualTo(game.word());
    }
}
