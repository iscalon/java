package com.nico.estimates;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

class EstimateTest {

    private Estimations estimations;

    @BeforeEach
    void init() {
        this.estimations = Estimations.of();
    }

    @Test
    @DisplayName("an estimation with B(est)=0, N(ormal)=0 and W(orst)=0 should have standard deviation (sigma) = 0.00 and expected completion time (mu) = 0.00")
    void test01() {
        addEstimation(0, 0, 0);

        assertStandardDeviationAndExpectedCompletionTime(0, 0);
    }

    @Test
    @DisplayName("an estimation with B(est)=0, N(ormal)=0 and W(orst)=1 should have standard deviation (sigma) = 0.17 and expected completion time (mu) = 0.17")
    void test02() {
        addEstimation(0, 0, 1);

        assertStandardDeviationAndExpectedCompletionTime(0.17d, 0.17d);
    }

    @Test
    @DisplayName("an estimation with B(est)=2, N(ormal)=5 and W(orst)=12 should have standard deviation (sigma) = 1.67 and expected completion time (mu) = 5.67")
    void test03() {
        addEstimation(2, 5, 12);

        assertStandardDeviationAndExpectedCompletionTime(1.67d, 5.67d);
    }

    @Test
    @DisplayName("an estimation with B(est)=2, N(ormal)=7 and W(orst)=28 should have standard deviation (sigma) = 4.33 and expected completion time (mu) = 9.67")
    void test04() {
        addEstimation(2, 7, 28);

        assertStandardDeviationAndExpectedCompletionTime(4.33d, 9.67d);
    }

    @Test
    @DisplayName("an estimation sum with no estimation has SIGMA = 0, MU = 0")
    void test05() {
        assertStandardDeviationAndExpectedCompletionTime(0, 0);
    }

    @Test
    @DisplayName("an estimation sum with one estimation (B=2, N=7, W=28) has SIGMA = 4.33, MU = 9.67")
    void test06() {
        addEstimation(2, 7, 28);

        assertStandardDeviationAndExpectedCompletionTime(4.33, 9.67);
    }

    @Test
    @DisplayName("an estimation sum with one estimation (B=2, N=7, W=28) and one estimation (B=2, N=5, W=12) has SIGMA = 4.64, MU = 15.34")
    void test07() {
        addEstimation(2, 7, 28);
        addEstimation(2, 5, 12);

        assertStandardDeviationAndExpectedCompletionTime(4.64, 15.34);
    }

    private void addEstimation(double best, double normal, double worst) {
        this.estimations.add(new Estimation(best, normal, worst));
    }

    private void assertStandardDeviationAndExpectedCompletionTime(double sigma, double mu) {
        BigDecimal roundedExpectedSigma = round(sigma);
        BigDecimal roundedExpectedMu = round(mu);

        assertThat(estimations.sigma())
                .isEqualTo(roundedExpectedSigma);
        assertThat(estimations.mu())
                .isEqualTo(roundedExpectedMu);
    }

    private BigDecimal round(double number) {
        return BigDecimal.valueOf(number).setScale(2, RoundingMode.HALF_UP);
    }
}
