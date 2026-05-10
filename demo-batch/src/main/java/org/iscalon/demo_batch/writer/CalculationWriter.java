package org.iscalon.demo_batch.writer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iscalon.demo_batch.domain.CalculatedResult;
import org.iscalon.demo_batch.domain.UserWorkUnit;
import org.iscalon.demo_batch.out.repository.CalculRepository;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class CalculationWriter implements ItemWriter<UserWorkUnit> {

  private final CalculRepository repository;

  @Override
  public void write(Chunk<? extends UserWorkUnit> chunk) {
    List<UserWorkUnit> users = List.copyOf(chunk.getItems());

    Set<Long> documentIds = new HashSet<>();

    for (UserWorkUnit user : users) {
      documentIds.addAll(user.references());
    }

    Map<Long, DocumentData> documentDataById = loadDocumentData(documentIds);

    List<CalculatedResult> results =
        users.stream()
            .map(user -> calculate(user, documentDataById))
            .flatMap(List::stream)
            .toList();

    repository.batchInsertResults(results);
  }

  private Map<Long, DocumentData> loadDocumentData(Set<Long> documentIds) {
    if (documentIds.isEmpty()) {
      return Map.of();
    }
    Map<Long, DocumentData> documentDataById = new HashMap<>();
    documentIds.forEach(
        documentId ->
            documentDataById.put(
                documentId, new DocumentData(ThreadLocalRandom.current().nextDouble(1, 1000))));
    return documentDataById;
  }

  private List<CalculatedResult> calculate(
      UserWorkUnit user, Map<Long, DocumentData> documentDataById) {
    BigDecimal total = BigDecimal.ZERO;
    log.info("Traitement de : {}", user);
    List<CalculatedResult> results = new ArrayList<>();
    for (Long documentId : user.references()) {
      DocumentData data = documentDataById.get(documentId);

      if (data != null) {
        total = total.add(BigDecimal.valueOf(data.value()));
      }
      results.add(new CalculatedResult(user.userId(), documentId, total));
    }

    return results;
  }

  record DocumentData(double value) {}
}
