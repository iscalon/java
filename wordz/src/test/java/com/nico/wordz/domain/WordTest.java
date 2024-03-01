package com.nico.wordz.domain;

import static com.nico.wordz.domain.LetterState.CORRECT;
import static com.nico.wordz.domain.LetterState.INCORRECT;
import static com.nico.wordz.domain.LetterState.PARTIALLY_CORRECT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import com.nico.wordz.domain.exceptions.IllegalWordCandidateSizeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WordTest {

  @Test
  @DisplayName("returns 'incorrect' score when one letter is incorrect")
  void test01() {
    Word word = new Word("A");

    WordMatchingScore wordMatchingScore = word.guess("Z");

    List<LetterState> candidateLetterStates = wordMatchingScore.letterStates();
    assertThat(candidateLetterStates).containsExactly(INCORRECT);
  }

  @Test
  @DisplayName("returns 'correct' score when one letter is correct")
  void test02() {
    Word word = new Word("A");

    WordMatchingScore wordMatchingScore = word.guess("A");

    List<LetterState> candidateLetterStates = wordMatchingScore.letterStates();
    assertThat(candidateLetterStates).containsExactly(CORRECT);
  }

  @Test
  @DisplayName("returns 'partially_correct' score for a second letter in an incorrect position")
  void test03() {
    Word word = new Word("AR");

    WordMatchingScore wordMatchingScore = word.guess("ZA");

    List<LetterState> candidateLetterStates = wordMatchingScore.letterStates();
    assertThat(candidateLetterStates).containsExactly(INCORRECT, PARTIALLY_CORRECT);
  }

  @Test
  @DisplayName("all letter states combinations found")
  void test04() {
    Word word = new Word("DBZ");

    WordMatchingScore wordMatchingScore = word.guess("RDZ");

    List<LetterState> candidateLetterStates = wordMatchingScore.letterStates();
    assertThat(candidateLetterStates).containsExactly(INCORRECT, PARTIALLY_CORRECT, CORRECT);
  }

  @Test
  @DisplayName("word guess must have the same size as correct word, not greater")
  void test05() {
    Word word = new Word("TURTLE");

    assertThatThrownBy(() -> word.guess("BLACKCAT"))
        .isInstanceOf(IllegalWordCandidateSizeException.class);
  }

  @Test
  @DisplayName("word guess must have the same size as correct word, not lower")
  void test06() {
    Word word = new Word("TURTLE");

    assertThatThrownBy(() -> word.guess("CAT"))
        .isInstanceOf(IllegalWordCandidateSizeException.class);
  }

  @Test
  @DisplayName("word to guess must not be null")
  void test07() {
    Word word = new Word(null);

    assertThatThrownBy(() -> word.guess("UNIT")).isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("guess must not be null")
  void test08() {
    Word word = new Word("EMP");

    assertThatThrownBy(() -> word.guess(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("word to guess must not be empty")
  void test09() {
    Word word = new Word("");

    assertThatThrownBy(() -> word.guess("UNIT")).isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("guess must not be empty")
  void test10() {
    Word word = new Word("XYZ");

    assertThatThrownBy(() -> word.guess("")).isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("word to guess must not be blank")
  void test11() {
    Word word = new Word("    "); // 4 spaces

    assertThatThrownBy(() -> word.guess("UNIT")).isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("guess must not be blank")
  void test12() {
    Word word = new Word("XYZ");

    assertThatThrownBy(() -> word.guess("    ")) // 4 spaces
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("case is insensitive")
  void test13() {
    Word word = new Word("xYzT");

    WordMatchingScore wordMatchingScore = word.guess("ZyXo");

    List<LetterState> candidateLetterStates = wordMatchingScore.letterStates();
    assertThat(candidateLetterStates)
        .containsExactly(PARTIALLY_CORRECT, CORRECT, PARTIALLY_CORRECT, INCORRECT);
  }
}
