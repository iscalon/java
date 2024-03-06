package com.nico.sorts;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SortTest {

    @Test
    @DisplayName("sort an empty list should return an empty list")
    void test01() {
        assertThat(Sorts.sort(List.of()))
                .isEmpty();
    }

    @Test
    @DisplayName("sort a one element list should return the same list")
    void test02() {
        assertThat(Sorts.sort(List.of(7)))
                .containsExactly(7);
    }

    @Test
    @DisplayName("sort a reverse ordered two element list should return reversed input list")
    void test03() {
        assertThat(Sorts.sort(List.of(2, 1)))
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("sort an out of order list should return ordered list")
    void test04() {
        assertThat(Sorts.sort(List.of(2, 3, 1)))
                .containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("sort a reverse ordered three element list should return reversed input list")
    void test05() {
        assertThat(Sorts.sort(List.of(4, 3, 2, 1)))
                .containsExactly(1, 2, 3, 4);
    }
}
