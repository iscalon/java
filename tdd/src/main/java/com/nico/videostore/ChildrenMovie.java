package com.nico.videostore;

public class ChildrenMovie extends Movie {

    public ChildrenMovie(String title) {
        super(title);
    }

    @Override
    public int getPoints(int days) {
        return 1;
    }

    @Override
    public double getFee(int days) {
        return 1.00d * days;
    }
}
