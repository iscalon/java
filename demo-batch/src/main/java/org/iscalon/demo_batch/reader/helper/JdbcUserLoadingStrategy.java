package org.iscalon.demo_batch.reader.helper;

import java.sql.Array;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iscalon.demo_batch.domain.UserWorkUnit;
import org.iscalon.demo_batch.reader.UserLoadingStrategy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class JdbcUserLoadingStrategy implements UserLoadingStrategy {

  private final JdbcTemplate jdbcTemplate;

  @Override
  public String key() {
    return "JDBC_LOADER";
  }

  @Override
  public List<UserWorkUnit> load(int bucket, int bucketCount, int pageSize, String lastUserId) {
    log.info(
        "[ACCES BDD] : Chargement de {} nouveaux utilisateurs à partir de l'id : {}",
        pageSize,
        lastUserId);
    String sql =
        """
                  SELECT
                      user_id,
                      array_agg(v_ref ORDER BY v_ref) AS document_ids
                  FROM source_data
                  WHERE MOD(ABS(ORA_HASH(user_id)), ?) = ?
                    AND (? IS NULL OR user_id > ?)
                  GROUP BY user_id
                  ORDER BY user_id
                  LIMIT ?
                  """;

    return jdbcTemplate.query(
        sql,
        ps -> {
          ps.setInt(1, bucketCount);
          ps.setInt(2, bucket);
          ps.setString(3, lastUserId);
          ps.setString(4, lastUserId);
          ps.setInt(5, pageSize);
        },
        (rs, rowNum) -> {
          String userId = rs.getString("user_id");

          Array sqlArray = rs.getArray("document_ids");
          Long[] documentIds =
              Stream.of((Object[]) sqlArray.getArray())
                  .map(v -> ((Number) v).longValue())
                  .toArray(Long[]::new);

          return new UserWorkUnit(userId, Set.of(documentIds));
        });
  }
}
