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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GuessTest {

    @Mock
    private GameRepository games;
    @Mock
    private RandomNumbers randomNumbers;
    @Mock
    private WordRepository words;

    @InjectMocks
    private Wordz wordz;

    @Test
    @DisplayName("guess with first misplaced letter")
    void test01() {
        Player player = new Player("Don");
        Word word = new Word("PIZZA");
        Word wordCandidate = new Word("ZOOLL");
        Game game = Game.create(word, player);
        forcePlayerGameFetchTo(game, player);

        GuessResult result = wordz.assess(player, wordCandidate);

        LetterState firstLetterState = result.score().letterStateAt(0);
        assertThat(firstLetterState)
                .isEqualTo(LetterState.PARTIALLY_CORRECT);
    }

    @Test
    @DisplayName("guess attempt will increment attempt counter")
    void test02() {
        Player player = new Player("Don");
        Word word = new Word("PIZZA");
        Game game = Game.create(word, player);
        forcePlayerGameFetchTo(game, player);

        game = makeAttempts(player,2);

        assertThat(game.attemptNumber())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("all letters correct")
    void test03a() {
        Player player = new Player("Don");
        Word word = new Word("PIZZA");
        Word wordCandidate = new Word("PIZZA");
        Game game = Game.create(word, player);
        forcePlayerGameFetchTo(game, player);

        GuessResult result = wordz.assess(player, wordCandidate);

        WordMatchingScore score = result.score();
        assertThat(score.allMatching()).isTrue();
    }

    @Test
    @DisplayName("not all letters correct")
    void test03b() {
        Player player = new Player("Don");
        Word word = new Word("PIZZA");
        Word wordCandidate = new Word("PIZLA");
        Game game = Game.create(word, player);
        forcePlayerGameFetchTo(game, player);

        GuessResult result = wordz.assess(player, wordCandidate);

        WordMatchingScore score = result.score();
        assertThat(score.allMatching()).isFalse();
    }

    @Test
    @DisplayName("correct guess should over the game")
    void test03() {
        Player player = new Player("Don");
        Word word = new Word("PIZZA");
        Word wordCandidate = new Word("PIZZA");
        Game game = Game.create(word, player);
        forcePlayerGameFetchTo(game, player);

        GuessResult result = wordz.assess(player, wordCandidate);

        assertThat(result.isGameOver()).isTrue();
    }

    @Test
    @DisplayName("5 unsuccessful guess attempts for a 5 letters word won't over the game")
    void test04() {
        Player player = new Player("Don");
        Word word = new Word("PIZZA");
        Game game = Game.create(word, player);
        forcePlayerGameFetchTo(game, player);

        makeAttempts(player,4);
        GuessResult result = wordz.assess(player, new Word("XXXXX"));

        assertThat(result.isGameOver())
                .isFalse();
    }

    @Test
    @DisplayName("6 unsuccessful guess attempts for a 5 letters word will over the game")
    void test05() {
        Player player = new Player("Don");
        Word word = new Word("PIZZA");
        Game game = Game.create(word, player);
        forcePlayerGameFetchTo(game, player);

        makeAttempts(player,5);
        GuessResult result = wordz.assess(player, new Word("XXXXX"));

        assertThat(result.isGameOver())
                .isTrue();
    }


    @Test
    @DisplayName("reject guess after game over")
    void test06() {
        Player player = new Player("Don");
        Word word = new Word("PIZZA");
        Word correctWord = new Word("PIZZA");
        Word anotherWord = new Word("NONON");
        Game game = Game.create(word, player);
        forcePlayerGameFetchTo(game, player);

        game = makeAttempts(player,1, correctWord);

        assertThat(game.isGameOver()).isTrue();
        assertThatThrownBy(() -> wordz.assess(player, anotherWord))
                .hasMessage("Game is already over");
    }

    private void forcePlayerGameFetchTo(Game game, Player player) {
        lenient().when(games.findByPlayer(player))
                .thenReturn(Optional.of(game));
    }

    private Game makeAttempts(Player player, int times) {
        return makeAttempts(player, times, null);
    }

    private Game makeAttempts(Player player, int times, Word wordCandidate) {
        wordCandidate = Optional.ofNullable(wordCandidate)
                .orElseGet(() -> new Word("NUULL"));
        Game updatedGame = null;
        for(int i = 1 ; i <= times ; i++) {
            wordz.assess(player, wordCandidate);
            updatedGame = getUpdatedGameInRepository(i);
            forcePlayerGameFetchTo(updatedGame, player);
        }
        return updatedGame;
    }

    private Game getUpdatedGameInRepository(int times) {
        ArgumentCaptor<Game> argument = ArgumentCaptor.forClass(Game.class);
        verify(games, times(times)).update(argument.capture());
        return argument.getValue();
    }
}
