package org.iscalon.demo_batch;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;
import org.iscalon.demo_batch.annotation.BatchIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@BatchIntegrationTest
class DemoBatchApplicationTests {

  @Autowired private JobOperatorTestUtils jobTestUtils;

  @Test
  void should_run_job() throws Exception {
    JobParameters params =
        new JobParametersBuilder().addString("date", "2026-12-10").toJobParameters();

    JobExecution execution = jobTestUtils.startJob(params);

    assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
  }
}
