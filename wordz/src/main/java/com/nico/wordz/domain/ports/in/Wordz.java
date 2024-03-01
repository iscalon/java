package com.nico.wordz.domain.ports.in;

import com.nico.wordz.domain.Game;
import com.nico.wordz.domain.GuessResult;
import com.nico.wordz.domain.Player;
import com.nico.wordz.domain.Word;
import com.nico.wordz.domain.WordSelection;
import com.nico.wordz.domain.exceptions.GameAlreadyStartedException;
import com.nico.wordz.domain.exceptions.GameOverException;
import com.nico.wordz.domain.exceptions.NoGameStartedException;
import com.nico.wordz.domain.ports.out.GameRepository;
import com.nico.wordz.domain.ports.out.RandomNumbers;
import com.nico.wordz.domain.ports.out.WordRepository;

import static java.util.Objects.requireNonNull;

public class Wordz implements WordzPort {

    private final GameRepository games;
    private final WordSelection wordSelection;

    public Wordz(GameRepository games, RandomNumbers randomNumbers, WordRepository words) {
        this.games = requireNonNull(games);
        this.wordSelection = new WordSelection(
                requireNonNull(words),
                requireNonNull(randomNumbers));
    }

    @Override
    public void newGame(Player player) {
        if(games.findByPlayer(player).isPresent()) {
            throw new GameAlreadyStartedException();
        }
        Word word = wordSelection.getRandomWord();
        Game game = Game.create(word, player);
        games.create(game);
    }

    @Override
    public GuessResult assess(Player player, Word wordCandidate) {
        Game game = getCurrentUnfinishedGame(player);
        game = game.withAttempt(wordCandidate);
        games.update(game);
        return new GuessResult(player, game.score(), game.isGameOver());
    }

    private Game getCurrentUnfinishedGame(Player player) {
    Game game =
        games
            .findByPlayer(player)
            .orElseThrow(() -> new NoGameStartedException(player));
        assertGameIsNotOver(game);
        return game;
    }

    private static void assertGameIsNotOver(Game game) {
        if(!game.isGameOver()) {
            return;
        }
        throw new GameOverException();
    }
}
