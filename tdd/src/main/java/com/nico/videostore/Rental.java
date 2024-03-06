package com.nico.videostore;

public record Rental(Movie movie, int days) {

  public Rental(String movieTitle, int days) {
    this(
        VideoRegistry.getMovie(movieTitle).orElseThrow(() -> new IllegalArgumentException("No movie found with title : " + movieTitle)), days);
  }

  public double getFee() {
    return movie().getFee(days);
  }

  public int getPoints() {
    return movie().getPoints(days);
  }
}
