package com.nico.wordz.database;

import com.nico.wordz.domain.Word;
import com.nico.wordz.domain.ports.out.WordRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "word")
public class WordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long number;
    private String text;

    protected WordEntity() {
        this(-1, "");
    }

    public WordEntity(long number, String text) {
        this.number = number;
        this.text = text;
    }

    public static WordEntity fromWord(Word word, WordRepository wordRepository) {
        if(word == null) {
            return null;
        }
        return new WordEntity(wordRepository.getHighestWordNumber() + 1L, word.text());
    }

    public Word toWord() {
        return new Word(text);
    }

    public long getId() {
        return id;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
