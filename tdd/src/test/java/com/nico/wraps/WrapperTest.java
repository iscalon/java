package com.nico.wraps;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class WrapperTest {

  public static final int MAX_WORD_SIZE = 10;
  public static final int MAX_COLUMN_WIDTH = 600;

  @Test
  @DisplayName("word wrapping an empty line should give a word wrapped empty list")
  void test01() {
    int columnWidth = ThreadLocalRandom.current().nextInt(1, 5);
    List<String> wordWrappedLine = Wrapper.wrap("", columnWidth);
    assertThat(wordWrappedLine)
            .isEmpty();
  }

  @Test
  @DisplayName("word wrapping a line with a 0 column width should give an empty list")
  void test02() {
    String line = generateLine();
    List<String> wordWrappedLine = Wrapper.wrap(line, 0);
    assertThat(wordWrappedLine)
            .isEmpty();
  }

  @Test
  @DisplayName("word wrapping a 1 character line with a 1 column width should give a list with one element equals to the line character")
  void test03() {
    String line = generateLine(1, 1);
    String firstCharacter = getCharacters(line).getFirst();
    List<String> wordWrappedLine = Wrapper.wrap(line, 1);

    assertThat(wordWrappedLine)
            .containsExactly(firstCharacter);
  }

  @Test
  @DisplayName("word wrapping a 2 characters line with a 1 column width should give an empty list")
  void test04() {
    String line = generateLine(1, 2);
    List<String> wordWrappedLine = Wrapper.wrap(line, 1);

    assertThat(wordWrappedLine)
            .isEmpty();
  }

  @Test
  @DisplayName("word wrapping a 2 characters line with a 2 column width should give a list containing one element equals to the line")
  void test05() {
    String line = generateLine(1, 2);
    List<String> wordWrappedLine = Wrapper.wrap(line, 2);

    assertThat(wordWrappedLine)
            .containsExactly(line);
  }

  @Test
  @DisplayName("word wrapping a two -1 character- words line with a 1 column width should give a list containing two elements each respectively equals to the line words")
  void test06() {
    String firstWord = "A";
    String secondWord = "B";
    String line = firstWord + " " + secondWord;
    List<String> wordWrappedLine = Wrapper.wrap(line, 1);

    assertThat(wordWrappedLine)
            .containsExactly(firstWord, secondWord);
  }

  @Test
  @DisplayName("word wrapping a two -1 character- words line with a 2 column width should give a list containing two elements each respectively equals to the line words")
  void test07() {
    String line = "A B";
    List<String> wordWrappedLine = Wrapper.wrap(line, 2);

    assertThat(wordWrappedLine)
            .containsExactly("A ", "B");
  }

  @Test
  @DisplayName("word wrapping a two -2 characters- words line with a 1 column width should give an empty list")
  void test08() {
    String firstWord = "AA";
    String secondWord = "BB";
    String line = firstWord + " " + secondWord;
    List<String> wordWrappedLine = Wrapper.wrap(line, 1);

    assertThat(wordWrappedLine)
            .isEmpty();
  }

  @Test
  @DisplayName("word wrapping a two -2 characters- words line with a 2 column width should give a list containing two elements each respectively equals to the line words")
  void test09() {
    String firstWord = "AA";
    String secondWord = "BB";
    String line = firstWord + " " + secondWord;
    List<String> wordWrappedLine = Wrapper.wrap(line, 2);

    assertThat(wordWrappedLine)
            .containsExactly(firstWord, secondWord);
  }

  @Test
  @DisplayName("word wrapping a three -2 characters- words line with a 2 column width should give a list containing two elements each respectively equals to the line words")
  void test10() {
    String firstWord = "AA";
    String secondWord = "BB";
    String thirdWord = "CC";
    String line = firstWord + " " + secondWord + " " + thirdWord;
    List<String> wordWrappedLine = Wrapper.wrap(line, 2);

    assertThat(wordWrappedLine)
            .containsExactly(firstWord, secondWord, thirdWord);
  }

  @Test
  @DisplayName("x x x - 3 -> x x<break>x")
  void test11() {
    String line = "x x x";

    List<String> wordWrappedList = Wrapper.wrap(line,3);

    assertThat(wordWrappedList)
            .containsExactly("x x", "x");
  }

  @Test
  @DisplayName("Four score and seven years ago I was a jedi - 15 -> Four score and <break>seven years ago<break>I was a jedi")
  void test12() {
    String line = "Four score and seven years ago I was a jedi";

    List<String> wordWrappedList = Wrapper.wrap(line,15);

    assertThat(wordWrappedList)
            .containsExactly("Four score and ", "seven years ago", "I was a jedi");
  }



  private String generateLine() {
    return generateLine(5);
  }

  private String generateLine(int wordsNumber) {
    return IntStream.rangeClosed(1, wordsNumber)
            .mapToObj(wordNumber -> generateLine(1, ThreadLocalRandom.current().nextInt(1, MAX_WORD_SIZE + 1)))
            .collect(Collectors.joining(" "));
  }

  private String generateLine(int wordsNumber, int wordSize) {
    if(wordsNumber <= 0) {
      wordsNumber = 1;
    }
    return IntStream.rangeClosed(1, wordsNumber)
            .mapToObj(wordNumber -> RandomStringUtils.randomAlphabetic(wordSize <= 0 ? 1 : wordSize))
            .collect(Collectors.joining(" "));
  }

  private List<String> getWords(String line) {
    return Arrays.stream(line.split("\\s")).toList();
  }

  private List<String> getCharacters(String word) {
    return word.chars().mapToObj(c -> "" + (char) c).toList();
  }
}
