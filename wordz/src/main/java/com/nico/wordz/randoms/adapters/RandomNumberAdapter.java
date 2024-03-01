package com.nico.wordz.randoms.adapters;

import com.nico.wordz.domain.ports.out.RandomNumbers;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
class RandomNumberAdapter implements RandomNumbers {

    static RandomNumberAdapter create() {
        return new RandomNumberAdapter();
    }

    @Override
    public int nextInt(int upperBoundInclusive) {
        if(upperBoundInclusive <= 0) {
            return 0;
        }
        int origin = 1;
        int bound = upperBoundInclusive + 1;
        return ThreadLocalRandom.current()
                .nextInt(origin, bound);
    }
}
