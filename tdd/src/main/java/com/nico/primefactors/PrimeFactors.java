package com.nico.primefactors;

import java.util.ArrayList;
import java.util.List;

public class PrimeFactors {

    private PrimeFactors() {
        // Does nothing
    }

    public static List<Integer> of(int n) {
        if(n < 0) {
            n = Math.abs(n);
        }
        if(n == 0 || n == 1) {
            return List.of(n);
        }
        ArrayList<Integer> factors = new ArrayList<>();
        for (int divisor = 2; n > 1; divisor++)
            for (; n % divisor == 0; n /= divisor)
                factors.add(divisor);
        return factors;
    }
}
