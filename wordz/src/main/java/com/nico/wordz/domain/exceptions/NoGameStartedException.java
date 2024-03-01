package com.nico.wordz.domain.exceptions;

import com.nico.wordz.domain.Player;

import java.util.Optional;

public class NoGameStartedException extends RuntimeException {

    public NoGameStartedException(Player player) {
        super("No game started for player : " + Optional.ofNullable(player).map(Player::name).orElse(null));
    }
}
