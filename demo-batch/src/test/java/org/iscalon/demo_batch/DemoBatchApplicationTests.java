package org.iscalon.demo_batch;

import lombok.extern.slf4j.Slf4j;
import org.iscalon.demo_batch.annotation.BatchIntegrationTest;
import org.junit.jupiter.api.Test;

@Slf4j
@BatchIntegrationTest
class DemoBatchApplicationTests {

  @Test
  void contextLoads() {
    log.info("Spring BatchJdbcH2Application finished");
  }
}
