package com.nico.wordz.domain;

public record Word(String text) {

  WordMatchingScore guess(String wordCandidate) {
    return new WordMatchingScore(text, wordCandidate);
  }
}
