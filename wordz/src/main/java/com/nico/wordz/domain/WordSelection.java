package com.nico.wordz.domain;

import com.nico.wordz.domain.exceptions.WordSelectionException;
import com.nico.wordz.domain.ports.out.RandomNumbers;
import com.nico.wordz.domain.ports.out.WordRepository;

import static java.util.Objects.requireNonNull;

public class WordSelection {

    private final WordRepository words;
    private final RandomNumbers randoms;

    public WordSelection(WordRepository words, RandomNumbers randoms) {
        this.words = requireNonNull(words);
        this.randoms = requireNonNull(randoms);
    }

    public Word getRandomWord() {
        int highestWordNumber = words.getHighestWordNumber();
        int number = randoms.nextInt(highestWordNumber);
        return words.findWordByNumber(number)
                .orElseThrow(() -> new WordSelectionException("No available word to select"));
    }
}
