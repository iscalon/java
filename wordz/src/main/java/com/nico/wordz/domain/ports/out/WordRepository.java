package com.nico.wordz.domain.ports.out;

import com.nico.wordz.domain.Word;

import java.util.Optional;

public interface WordRepository {
    Optional<Word> findWordByNumber(int number);

    int getHighestWordNumber();

    Word createWord(String text);
}
