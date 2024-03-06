package com.nico.videostore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VideoRegistry {

  private static final Map<String, VideoType> REGISTRY = new HashMap<>();

  private VideoRegistry() {
    // Does nothing
  }

  public static Optional<Movie> getMovie(String title) {
    return Optional.ofNullable(REGISTRY.get(title))
        .map(
            type ->
              switch (type) {
                case REGULAR -> new RegularMovie(title);
                case CHILDREN -> new ChildrenMovie(title);
              });
  }

  public static void addMovie(String title, VideoType videoType) {
    REGISTRY.put(title,videoType);
  }
}
