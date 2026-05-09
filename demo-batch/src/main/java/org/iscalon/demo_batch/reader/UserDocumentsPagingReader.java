package org.iscalon.demo_batch.reader;

import java.sql.Array;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.iscalon.demo_batch.domain.UserWorkUnit;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamReader;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
public class UserDocumentsPagingReader implements ItemStreamReader<UserWorkUnit> {

  private static final String LAST_USER_ID_KEY = "lastUserId";

  private final JdbcTemplate jdbcTemplate;
  private final int bucket;
  private final int bucketCount;
  private final int pageSize;

  private final Queue<UserWorkUnit> buffer = new ArrayDeque<>();

  private String lastUserId;
  private boolean finished;

  public UserDocumentsPagingReader(
      JdbcTemplate jdbcTemplate, int bucket, int bucketCount, int pageSize) {
    this.jdbcTemplate = jdbcTemplate;
    this.bucket = bucket;
    this.bucketCount = bucketCount;
    this.pageSize = pageSize;
  }

  @Override
  public UserWorkUnit read() {
    if (buffer.isEmpty() && !finished) {
      loadNextPage();
    }

    UserWorkUnit next = buffer.poll();
    log.info("Ajout de : {} au chunck", next);
    if (next != null) {
      lastUserId = next.userId();
    }

    return next;
  }

  private void loadNextPage() {
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

    List<UserWorkUnit> page =
        jdbcTemplate.query(
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

    if (page.isEmpty()) {
      finished = true;
    } else {
      buffer.addAll(page);
    }
  }

  @Override
  public void open(ExecutionContext executionContext) {
    if (executionContext.containsKey(LAST_USER_ID_KEY)) {
      this.lastUserId = executionContext.getString(LAST_USER_ID_KEY);
    }
  }

  @Override
  public void update(ExecutionContext executionContext) {
    if (lastUserId != null) {
      executionContext.putString(LAST_USER_ID_KEY, lastUserId);
    }
  }

  @Override
  public void close() {
    buffer.clear();
  }
}
