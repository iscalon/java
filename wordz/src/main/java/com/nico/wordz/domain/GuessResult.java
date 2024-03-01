package com.nico.wordz.domain;

public record GuessResult(Player player, WordMatchingScore score, boolean isGameOver) {
}
