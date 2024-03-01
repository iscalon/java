package com.nico.wordz.domain.exceptions;

public class IllegalWordCandidateSizeException extends RuntimeException {

    public IllegalWordCandidateSizeException(String wordToGuess, String wordCandidate) {
        super("Word to guess has size : " + wordToGuess.length() + ", whereas guess has size : " + wordCandidate.length());
    }
}
