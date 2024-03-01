package com.nico.wordz.domain;

import com.nico.wordz.domain.exceptions.WordSelectionException;
import com.nico.wordz.domain.ports.out.RandomNumbers;
import com.nico.wordz.domain.ports.out.WordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordSelectionTest {

    private static final int HIGHEST_WORD_NUMBER = 5;
    private static final int TURTLE_WORD_NUMBER = 4;
    private static final int GOOD_WORD_NUMBER = 3;
    private static final int NO_WORD_NUMBER = 2;

    @Mock
    private WordRepository words;

    @Mock
    private RandomNumbers randoms;

    @BeforeEach
    void initWordsRepository() {
        lenient().when(words.getHighestWordNumber())
                .thenReturn(HIGHEST_WORD_NUMBER);

        lenient().when(words.findWordByNumber(NO_WORD_NUMBER))
                .thenReturn(Optional.empty());

        lenient().when(words.findWordByNumber(GOOD_WORD_NUMBER))
                .thenReturn(Optional.of(new Word("GOOD")));

        lenient().when(words.findWordByNumber(TURTLE_WORD_NUMBER))
                .thenReturn(Optional.of(new Word("TURTLE")));
    }

    @Test
    @DisplayName("should throw a word selection exception if word repository search throws a word selection exception")
    void test01() {
        doThrow(new WordSelectionException())
                .when(words).findWordByNumber(anyInt());

        WordSelection selection = new WordSelection(words, randoms);

        assertThatThrownBy(selection::getRandomWord)
                .isInstanceOf(WordSelectionException.class);
    }

    @Test
    @DisplayName("should get a word selection at a given position")
    void test02() {
        when(randoms.nextInt(HIGHEST_WORD_NUMBER))
                .thenReturn(GOOD_WORD_NUMBER);
        WordSelection selection = new WordSelection(words, randoms);

        Word selectedWord = selection.getRandomWord();

        assertThat(selectedWord)
                .isEqualTo(new Word("GOOD"));
    }

    @Test
    @DisplayName("should throw a word selection exception if no word is found in repository")
    void test03() {
        when(randoms.nextInt(HIGHEST_WORD_NUMBER))
                .thenReturn(NO_WORD_NUMBER);
        WordSelection selection = new WordSelection(words, randoms);

        assertThatThrownBy(selection::getRandomWord)
                .isInstanceOf(WordSelectionException.class);
    }
}
