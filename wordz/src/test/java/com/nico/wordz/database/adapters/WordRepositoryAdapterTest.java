package com.nico.wordz.database.adapters;

import com.nico.wordz.database.WordEntity;
import com.nico.wordz.domain.Word;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tooling.JpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JpaTest
class WordRepositoryAdapterTest {

  @Inject
  private WordRepositoryAdapter adapter;

  @Test
  @DisplayName("should find the highest word number")
  void test01() {
    adapter.save(new WordEntity(2, "TOTO"));
    adapter.save(new WordEntity(8, "TATA"));
    adapter.save(new WordEntity(5, "TUTU"));

    int highestWordNumber = adapter.getHighestWordNumber();

    assertThat(highestWordNumber)
            .isEqualTo(8);
  }

  @Test
  @DisplayName("should find word by number")
  void test02() {
    int wordNumber = adapter.getHighestWordNumber() + 1;
    WordEntity word = new WordEntity(wordNumber, "TOTO");
    adapter.save(word);

    Optional<Word> wordByNumber = adapter.findWordByNumber(wordNumber);

    assertThat(wordByNumber)
            .hasValue(new Word("TOTO"));
  }
}
