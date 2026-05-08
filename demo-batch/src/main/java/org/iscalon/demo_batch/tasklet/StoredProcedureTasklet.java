package org.iscalon.demo_batch.tasklet;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class StoredProcedureTasklet implements Tasklet {

  private final JdbcTemplate jdbcTemplate;
  private final String procedureCall;

  public StoredProcedureTasklet(JdbcTemplate jdbcTemplate, String procedureCall) {
    this.jdbcTemplate = jdbcTemplate;
    this.procedureCall = procedureCall;
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    jdbcTemplate.execute(procedureCall);
    return RepeatStatus.FINISHED;
  }
}
