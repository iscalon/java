package com.nico.wordz.rest.adapters;

import com.nico.wordz.domain.GuessResult;
import com.nico.wordz.domain.LetterState;
import com.nico.wordz.domain.Player;
import com.nico.wordz.domain.Word;
import com.nico.wordz.domain.exceptions.GameAlreadyStartedException;
import com.nico.wordz.domain.exceptions.GameOverException;
import com.nico.wordz.domain.exceptions.NoGameStartedException;
import com.nico.wordz.domain.exceptions.WordSelectionException;
import com.nico.wordz.domain.ports.in.Wordz;
import com.nico.wordz.domain.ports.in.WordzPort;
import com.nico.wordz.domain.ports.out.GameRepository;
import com.nico.wordz.domain.ports.out.RandomNumbers;
import com.nico.wordz.domain.ports.out.WordRepository;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static java.util.Objects.requireNonNull;

@RestController
@RequestMapping("/wordz")
class WordzRestAdapter {

    private final WordzPort wordz;
    private final WordRepository words;

    WordzRestAdapter(GameRepository games, RandomNumbers randoms, WordRepository words) {
        this.words = requireNonNull(words);
        this.wordz = new Wordz(requireNonNull(games), requireNonNull(randoms), words);
    }

    @PostMapping("/{playerName}")
    ResponseEntity<?> startNewGame(@PathVariable("playerName") String playerName) {
        wordz.newGame(new Player(playerName));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/word")
    ResponseEntity<?> addWord(@RequestBody CreateWordCommand command) {
        words.createWord(command.word());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{playerName}/guess")
    ResponseEntity<?> guess(@PathVariable("playerName") String playerName, @RequestBody CreateGuessCommand command) {
        GuessResult result = wordz.assess(new Player(playerName), new Word(command.text()));
        GuessResultRepresentation resultRepresentation = GuessResultRepresentation.of(result);
        return ResponseEntity.ok(resultRepresentation);
    }

    @SuppressWarnings("java:S6218")
    record GuessResultRepresentation(String player, String[] score, boolean isGameOver) {
        static GuessResultRepresentation of(GuessResult result) {
            String playerName = result.player().name();
            String[] score = result.score().letterStates().stream().map(LetterState::name).toArray(String[]::new);
            return new GuessResultRepresentation(playerName, score, result.isGameOver());
        }
    }

    record CreateWordCommand(String word) {}

    record CreateGuessCommand(String text) {}

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @RestControllerAdvice
    public static class RestExceptionHandler extends ResponseEntityExceptionHandler {

        @ExceptionHandler(WordSelectionException.class)
        protected ResponseEntity<?> handleWordSelectionException(WordSelectionException ex) {
            return ResponseEntity.internalServerError()
                    .body(ex.getLocalizedMessage());
        }

        @ExceptionHandler(GameAlreadyStartedException.class)
        protected ResponseEntity<?> handleGameAlreadyStartedException(GameAlreadyStartedException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ex.getLocalizedMessage());
        }

        @ExceptionHandler(NoGameStartedException.class)
        protected ResponseEntity<?> handleNoGameStartedException(NoGameStartedException ex) {
            return ResponseEntity.internalServerError()
                    .body(ex.getLocalizedMessage());
        }

        @ExceptionHandler(GameOverException.class)
        protected ResponseEntity<?> handleGameOverException(GameOverException ex) {
            return ResponseEntity.internalServerError()
                    .body(ex.getLocalizedMessage());
        }
    }

}
