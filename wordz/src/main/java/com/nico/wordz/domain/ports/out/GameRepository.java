package com.nico.wordz.domain.ports.out;

import com.nico.wordz.domain.Game;
import com.nico.wordz.domain.Player;

import java.util.Optional;

public interface GameRepository {
    void create(Game game);

    Optional<Game> findByPlayer(Player player);

    void update(Game game);
}
