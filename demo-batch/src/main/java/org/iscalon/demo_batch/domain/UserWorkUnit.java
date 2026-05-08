package org.iscalon.demo_batch.domain;

import java.util.Optional;
import java.util.Set;

public record UserWorkUnit(String userId, Set<String> references) {

  public UserWorkUnit {
    references = Optional.ofNullable(references).orElseGet(Set::of);
  }
}
