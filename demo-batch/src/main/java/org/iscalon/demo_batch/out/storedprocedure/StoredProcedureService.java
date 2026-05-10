package org.iscalon.demo_batch.out.storedprocedure;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class StoredProcedureService implements PourAppelerStoredProcedure {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void appeler(String nomProcedure) {
    jdbcTemplate.execute(nomProcedure);
  }
}
