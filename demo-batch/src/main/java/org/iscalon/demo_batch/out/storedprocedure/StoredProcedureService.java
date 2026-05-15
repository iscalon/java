package org.iscalon.demo_batch.out.storedprocedure;

import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
class StoredProcedureService implements PourAppelerStoredProcedure {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public void appeler(String nomProcedure) {
    Map<
            String /* identifiant du result set */,
            Object /* LinkedMap : nom proc stockée -> chaine résultat */>
        resultats = new ProcedureInitialisation(jdbcTemplate, nomProcedure).execute();
    if (log.isDebugEnabled()) {
      String informations =
          resultats.values().stream().map(Object::toString).collect(Collectors.joining(","));
      log.debug("Résultats renvoyés par appel procédure '{}' : {}", nomProcedure, informations);
    }
  }
}
