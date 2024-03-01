package com.nico.wordz.domain.exceptions;

public class WordSelectionException extends RuntimeException {

    public WordSelectionException() {
        this("");
    }

    public WordSelectionException(String message) {
        super(message);
    }
}
