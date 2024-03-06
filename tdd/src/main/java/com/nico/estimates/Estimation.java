package com.nico.estimates;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Estimation(BigDecimal best, BigDecimal normal, BigDecimal worst) {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final BigDecimal SIX = BigDecimal.valueOf(6);

    public Estimation(double best, double normal, double worst) {
        this(createBigDecimal(best), createBigDecimal(normal), createBigDecimal(worst));
    }

    private static BigDecimal createBigDecimal(double best) {
        return BigDecimal.valueOf(best).setScale(SCALE, ROUNDING_MODE);
    }

    public BigDecimal sigma() {
        return worst().subtract(best())
                .divide(SIX, SCALE, ROUNDING_MODE);
    }

    public BigDecimal mu() {
        BigDecimal normalBy4 = normal().multiply(BigDecimal.valueOf(4));
        return best().add(normalBy4).add(worst())
                .divide(SIX, SCALE, ROUNDING_MODE);
    }

    @Override
    public String toString() {
        return "Estimation{" +
                "best=" + best +
                ", normal=" + normal +
                ", worst=" + worst +
                ", μ=" + mu() +
                ", σ=" + sigma() +
                '}';
    }
}
