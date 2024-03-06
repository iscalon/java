package com.nico.videostore;

public class RegularMovie extends Movie {

    public RegularMovie(String title) {
        super(title);
    }

    @Override
    public int getPoints(int days) {
        return getEffectiveRentalDays(days);
    }

    @Override
    public double getFee(int days) {
        return 1.50d * getEffectiveRentalDays(days);
    }

    public int getEffectiveRentalDays(int days) {
        return Math.max(days - 2, 1);
    }
}
