package com.nico.videostore;

public abstract class Movie {
    private final String title;

    protected Movie(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public abstract int getPoints(int days);

    public abstract double getFee(int days);
}
