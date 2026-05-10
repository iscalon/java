package org.iscalon.demo_batch.out.repository;

import java.util.List;
import org.iscalon.demo_batch.domain.CalculatedResult;

public interface CalculRepository {

  void batchInsertResults(List<CalculatedResult> results);
}
