package com.nico.wordz.database;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "game")
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String word;
    private long attemptNumber;
    private boolean gameOver;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private PlayerEntity player;

    protected GameEntity() {
        this(null, 0, false, null);
    }

    public GameEntity(String word, int attemptNumber, boolean gameOver, PlayerEntity player) {
        this.word = word;
        this.attemptNumber = attemptNumber;
        this.gameOver = gameOver;
        this.player = player;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public long getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(long attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }
}
