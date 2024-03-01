package com.nico.wordz.domain.ports.out;

import com.nico.wordz.domain.Player;

import java.util.Optional;

public interface PlayerRepository {
    Player create(String name);

    Optional<Player> findByName(String name);
}
