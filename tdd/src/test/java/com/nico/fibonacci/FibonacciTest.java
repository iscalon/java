package com.nico.fibonacci;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class FibonacciTest {

    @Test
    @DisplayName("fib(0) = 1")
    void test01() {
        assertThat(Fibonacci.compute(0))
                .isEqualTo(BigInteger.ONE);
    }

    @Test
    @DisplayName("fib(1) = 1")
    void test02() {
        assertThat(Fibonacci.compute(1))
                .isEqualTo(BigInteger.ONE);
    }

    @Test
    @DisplayName("fib(2) = 2")
    void test03() {
        assertThat(Fibonacci.compute(2))
                .isEqualTo(BigInteger.TWO);
    }

    @Test
    @DisplayName("fib(3) = 3")
    void test04() {
        assertThat(Fibonacci.compute(3))
                .isEqualTo(BigInteger.valueOf(3));
    }

    @Test
    @DisplayName("fib(100) = 573147844013817084101")
    void test05() {
        assertThat(Fibonacci.compute(100))
                .isEqualTo(new BigInteger("573147844013817084101"));
    }
}
