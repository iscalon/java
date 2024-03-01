package com.nico.wordz.database.adapters;

import com.nico.wordz.database.WordEntity;
import com.nico.wordz.domain.Word;
import com.nico.wordz.domain.ports.out.WordRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Component
class WordRepositoryAdapter implements WordRepository {

    private final WordJPARepository repository;

    WordRepositoryAdapter(WordJPARepository repository) {
        this.repository = requireNonNull(repository);
    }

    @Override
    public Optional<Word> findWordByNumber(int number) {
        return repository.findFirstByNumber(number)
                .map(WordEntity::toWord);
    }

    @Override
    public int getHighestWordNumber() {
        return repository.findTopByOrderByNumberDesc()
                .map(WordEntity::getNumber)
                .map(Long::intValue)
                .orElse(0);
    }

    @Override
    public Word createWord(String text) {
        repository.save(new WordEntity(getHighestWordNumber() + 1L, text));
        return new Word(text);
    }

    public WordEntity save(WordEntity word) {
        return repository.save(word);
    }

    interface WordJPARepository extends JpaRepository<WordEntity, Long> {
        Optional<WordEntity> findFirstByNumber(long number);

        Optional<WordEntity> findTopByOrderByNumberDesc();
    }
}
