package com.nico.bowling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class BowlingTest {

    private static final int MAX_NO_STRIKE_OR_SPARE_ROLLS = 20;

    private Game game;

    @BeforeEach
    void init() {
        game = new Game();
    }

    @Test
    @DisplayName("can create a game")
    void test01() {
        assertThat(game)
                .isNotNull();
    }

    @Test
    @DisplayName("can make a roll")
    void test02() {
        assertThatNoException()
                .isThrownBy(() -> game.roll(0));
    }

    @Test
    @DisplayName("when each roll ends in the gutter, then total score is : 0")
    void test03() {
        rollMany(MAX_NO_STRIKE_OR_SPARE_ROLLS,0);

        assertThat(game.score())
                .isZero();
    }

    @Test
    @DisplayName("when each roll downs only 1 pin, then total score should be : 20")
    void test04() {
        rollMany(MAX_NO_STRIKE_OR_SPARE_ROLLS,1);

        assertThat(game.score())
                .isEqualTo(20);
    }

    @Test
    @DisplayName("when one spare is performed followed by a '7' roll but all other roll ends in the gutter, then score should be : 24")
    void test05() {
        rollSpare();
        game.roll(7);
        rollMany(MAX_NO_STRIKE_OR_SPARE_ROLLS - 3, 0);

        assertThat(game.score())
                .isEqualTo(24);
    }

    @Test
    @DisplayName("when one strike is performed followed by a '2' and '3' rolls but all other roll ends in the gutter, then score should be : 20")
    void test06() {
        rollStrike();
        game.roll(2);
        game.roll(3);
        rollMany(MAX_NO_STRIKE_OR_SPARE_ROLLS - 4, 0);

        assertThat(game.score())
                .isEqualTo(20);
    }

    @Test
    @DisplayName("when perfect game, then score should be : 300")
    void test07() {
        perfectGame();

        assertThat(game.score())
                .isEqualTo(300);
    }

    private void perfectGame() {
        rollMany(10,10);
        game.roll(10);
    }

    private void rollSpare() {
        game.roll(0);
        game.roll(10);
    }

    private void rollStrike() {
        game.roll(10);
    }

    private void rollMany(int times, int pins) {
        for(int i = 0 ; i < times ; i++) {
            game.roll(pins);
        }
    }
}
