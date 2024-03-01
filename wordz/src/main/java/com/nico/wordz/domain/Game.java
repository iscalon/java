package com.nico.wordz.domain;

public record Game(Word word, int attemptNumber, Player player, boolean isGameOver, WordMatchingScore score) {

    public static final boolean GAME_OVER = true;
    public static final int START_ATTEMPT = 0;
    public static final WordMatchingScore NO_SCORE = null;

    public static Game create(Word word, Player player) {
        return new Game(word, START_ATTEMPT, player, !GAME_OVER, NO_SCORE);
    }

    public Game withAttempt(Word wordCandidate) {
        WordMatchingScore newScore = word.guess(wordCandidate.text());
        int incrementedAttempt = attemptNumber + 1;
        boolean isCorrectGuess = newScore.allMatching();
        boolean hasReachedMaxAttempts = incrementedAttempt >= getMaxAttempts();
        boolean isGameOver = isCorrectGuess || hasReachedMaxAttempts;
        return new Game(word, incrementedAttempt, player, isGameOver, newScore);
    }

    public int getMaxAttempts() {
        return word.text().length() + 1;
    }
}
