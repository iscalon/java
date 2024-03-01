package com.nico.wordz.database;

import com.nico.wordz.domain.Player;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "player")
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "player")
    private Collection<GameEntity> games;

    protected PlayerEntity() {
        this(null);
    }

    public PlayerEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public Collection<GameEntity> getGames() {
        return Optional.ofNullable(this.games)
                .map(List::copyOf)
                .orElseGet(List::of);
    }

    public Player toPlayer() {
        return new Player(getName());
    }

    public static PlayerEntity fromPlayer(Player player) {
        if (player == null) {
            return null;
        }
        return new PlayerEntity(player.name());
    }
}
