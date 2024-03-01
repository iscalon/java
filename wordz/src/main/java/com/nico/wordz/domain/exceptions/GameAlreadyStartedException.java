package com.nico.wordz.domain.exceptions;

public class GameAlreadyStartedException extends RuntimeException {

    public GameAlreadyStartedException() {
        super("A game is already in play");
    }
}
