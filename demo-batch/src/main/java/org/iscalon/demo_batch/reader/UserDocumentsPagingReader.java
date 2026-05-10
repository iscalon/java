package org.iscalon.demo_batch.reader;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import lombok.extern.slf4j.Slf4j;
import org.iscalon.demo_batch.domain.UserWorkUnit;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamReader;

@Slf4j
public class UserDocumentsPagingReader implements ItemStreamReader<UserWorkUnit> {

  private static final String LAST_USER_ID_KEY = "lastUserId";

  private final Queue<UserWorkUnit> buffer = new ArrayDeque<>();
  private final UserLoadingStrategy users;
  private final int bucket;
  private final int bucketCount;
  private final int pageSize;


  private String lastUserId;
  private boolean finished;

  public UserDocumentsPagingReader(
      UserLoadingStrategy users, int bucket, int bucketCount, int pageSize) {
    this.users = users;
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
    List<UserWorkUnit> page = users.load(bucket, bucketCount, pageSize, lastUserId);

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
