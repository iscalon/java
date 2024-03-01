package com.nico.wordz.domain;

import com.nico.wordz.domain.ports.in.Wordz;
import com.nico.wordz.domain.ports.out.GameRepository;
import com.nico.wordz.domain.ports.out.RandomNumbers;
import com.nico.wordz.domain.ports.out.WordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewGameTest {

    public static final int FORCED_RANDOM_NUMBER = 2;

    @Mock
    private GameRepository games;
    @Mock
    private RandomNumbers randomNumbers;
    @Mock
    private WordRepository words;

    @InjectMocks
    private Wordz wordz;

    @Test
    @DisplayName("a new game can be started by a player")
    void test01() {
        forceWordSelectionTo("ARISE");
        Player player = new Player("Leo");

        wordz.newGame(player);

        Game game = getCreatedGame();
        assertThat(game.word())
                .isEqualTo(new Word("ARISE"));
        assertThat(game.attemptNumber())
                .isZero();
        assertThat(game.player())
                .isSameAs(player);
    }

    @Test
    @DisplayName("new game starts with a random word selected")
    void test02() {
        forceWordSelectionTo("TURTLE");
        Player player = new Player("Leo");

        wordz.newGame(player);

        Game game = getCreatedGame();
        assertThat(game.word())
                .isEqualTo(new Word("TURTLE"));
    }

    private Game getCreatedGame() {
        ArgumentCaptor<Game> gameArgument = ArgumentCaptor.forClass(Game.class);
        verify(games).create(gameArgument.capture());
        return gameArgument.getValue();
    }

    private void forceWordSelectionTo(String text) {
        when(randomNumbers.nextInt(anyInt()))
                .thenReturn(FORCED_RANDOM_NUMBER);
        when(words.findWordByNumber(FORCED_RANDOM_NUMBER))
                .thenReturn(Optional.of(new Word(text)));
    }
}
