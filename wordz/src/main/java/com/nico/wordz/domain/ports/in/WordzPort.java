package com.nico.wordz.domain.ports.in;

import com.nico.wordz.domain.GuessResult;
import com.nico.wordz.domain.Player;
import com.nico.wordz.domain.Word;

public interface WordzPort {
  void newGame(Player player);

  GuessResult assess(Player player, Word wordCandidate);
}
