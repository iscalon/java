package org.iscalon.demo_batch.out.repository;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iscalon.demo_batch.domain.CalculatedResult;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class CalculJdbcRepository implements CalculRepository {

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Override
  @SuppressWarnings("unchecked")
  public void batchInsertResults(List<CalculatedResult> results) {
    if (results.isEmpty()) {
      log.info("Aucun enregistrement en masse");
      return;
    }
    log.info("[ACCES BDD] : Enregistrement en masse de : {}", results);
    String sql =
        """
                  INSERT INTO calculated_result(v_ref, user_id, calculated_amount)
                  VALUES (:vRef, :userId, :calculatedAmount)
                  """;

    Map<String, ?>[] batchParams =
        results.stream()
            .map(
                result ->
                    Map.of(
                        "vRef",
                        result.inputDataId(),
                        "userId",
                        result.userId(),
                        "calculatedAmount",
                        result.amount()))
            .toArray(Map[]::new);

    namedParameterJdbcTemplate.batchUpdate(sql, batchParams);
  }
}
