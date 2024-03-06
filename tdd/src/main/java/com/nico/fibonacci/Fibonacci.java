package com.nico.fibonacci;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;
import java.util.stream.IntStream;

public class Fibonacci {

    private Fibonacci() {
        // Does nothing
    }

    public static BigInteger compute(int rank) {
        if(rank < 2) {
            return BigInteger.ONE;
        }
        Pair<BigInteger, BigInteger> firstTwoTerms = Pair.of(BigInteger.ONE, BigInteger.ONE);
        return IntStream.rangeClosed(2, rank)
                .mapToObj(BigInteger::valueOf)
                .reduce(firstTwoTerms, Fibonacci::accumulate, (p1, p2) -> p2)
                .getRight(); // fib(rank)
    }

    private static Pair<BigInteger /* fib(n - 1) */, BigInteger /* fib(n) */> accumulate(
            Pair<BigInteger /* fib(n - 2) */, BigInteger /* fib(n - 1) */> lastTwoTerms, BigInteger rank) {
        BigInteger fibN0 = lastTwoTerms.getLeft();
        BigInteger fibN1 = lastTwoTerms.getRight();
        BigInteger fibN2 = fibN0.add(fibN1);

        return Pair.of(fibN1, fibN2);
    }
}
