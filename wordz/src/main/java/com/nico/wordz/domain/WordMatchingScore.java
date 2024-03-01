package com.nico.wordz.domain;

import static java.util.Objects.requireNonNull;

import com.nico.wordz.domain.exceptions.IllegalWordCandidateSizeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class WordMatchingScore {

  private final String wordToGuess;
  private final String wordCandidate;
  private final Set<Character> correctCharacters;

  WordMatchingScore(String wordToGuess, String wordCandidate) {
    this.wordToGuess = requireNonNull(wordToGuess).toUpperCase();
    this.wordCandidate = requireNonNull(wordCandidate).toUpperCase();
    new WordsValidations().assertCanComputeScore();
    this.correctCharacters = extractCorrectCharacters();
  }

  private static char codePointToCharacter(int codePoint) {
    return (char) codePoint;
  }

  private Set<Character> extractCorrectCharacters() {
    return this.wordToGuess
            .codePoints()
            .mapToObj(WordMatchingScore::codePointToCharacter)
            .collect(Collectors.toSet());
  }

  public List<LetterState> letterStates() {
    int wordToGuessLength = this.wordToGuess.length();
    List<LetterState> candidateLetterStates = new ArrayList<>(wordToGuessLength);
    for(int i = 0 ; i < wordToGuessLength ; i++) {
      candidateLetterStates.add(letterStateAt(i));
    }
    return candidateLetterStates;
  }

  public LetterState letterStateAt(int index) {
    if (areCandidateAndCorrectLettersMatchingAt(index)) {
      return LetterState.CORRECT;
    }
    if (doesWordToGuessContainCandidateLetterAt(index)) {
      return LetterState.PARTIALLY_CORRECT;
    }
    return LetterState.INCORRECT;
  }

  public boolean allMatching() {
    return letterStates().stream()
            .allMatch(state -> state == LetterState.CORRECT);
  }

  private boolean areCandidateAndCorrectLettersMatchingAt(int index) {
    char correctLetter = wordToGuess.charAt(index);
    char candidateLetter = wordCandidate.charAt(index);
    return Objects.equals(correctLetter, candidateLetter);
  }

  private boolean doesWordToGuessContainCandidateLetterAt(int index) {
    char candidateLetter = wordCandidate.charAt(index);
    return correctCharacters.contains(candidateLetter);
  }

  /**
   * Validations
   */
  private class WordsValidations {

    private static void assertNotBlank(String word) {
      if(StringUtils.isBlank(word)) {
        throw new IllegalStateException("Blank word");
      }
    }

    private static void assertNotEmpty(String word) {
      if(StringUtils.isEmpty(word)) {
        throw new IllegalStateException("Empty word");
      }
    }

    private static void assertSameSize(String wordToGuess, String wordCandidate) {
      if(wordToGuess.length() != wordCandidate.length()) {
        throw new IllegalWordCandidateSizeException(wordToGuess, wordCandidate);
      }
    }

    void assertCanComputeScore() {
      assertWordsNotEmpty();
      assertWordsNotBlank();
      assertWordsSameSize();
    }

    private void assertWordsNotBlank() {
      assertNotBlank(wordToGuess);
      assertNotBlank(wordCandidate);
    }

    private void assertWordsNotEmpty() {
      assertNotEmpty(wordToGuess);
      assertNotEmpty(wordCandidate);
    }

    private void assertWordsSameSize() {
      assertSameSize(wordToGuess, wordCandidate);
    }
  }
}
