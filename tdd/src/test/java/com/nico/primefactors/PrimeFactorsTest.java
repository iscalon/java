package com.nico.primefactors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrimeFactorsTest {

    @Test
    @DisplayName("prime factors for 1 should contain only : 1")
    void test01() {
        assertThat(PrimeFactors.of(1))
                .containsExactly(1);
    }

    @Test
    @DisplayName("prime factors for 2 should contain only : 2")
    void test02() {
        assertThat(PrimeFactors.of(2))
                .containsExactly(2);
    }

    @Test
    @DisplayName("prime factors for 3 should contain only : 3")
    void test03() {
        assertThat(PrimeFactors.of(3))
                .containsExactly(3);
    }

    @Test
    @DisplayName("prime factors for 4 should contain : {2, 2}")
    void test04() {
        assertThat(PrimeFactors.of(4))
                .containsExactly(2, 2);
    }

    @Test
    @DisplayName("prime factors for 418970771 should contain : {1129, 371099}")
    void test05() {
        assertThat(PrimeFactors.of(418970771))
                .containsExactly(1129, 371099);
    }
}
