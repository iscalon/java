package com.nico.wordz.domain.exceptions;

public class GameOverException extends RuntimeException {

    public GameOverException() {
        super("Game is already over");
    }
}
