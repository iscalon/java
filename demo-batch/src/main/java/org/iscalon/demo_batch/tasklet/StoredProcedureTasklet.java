package org.iscalon.demo_batch.tasklet;

import lombok.RequiredArgsConstructor;
import org.iscalon.demo_batch.out.storedprocedure.PourAppelerStoredProcedure;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

@RequiredArgsConstructor
public class StoredProcedureTasklet implements Tasklet {

  private final PourAppelerStoredProcedure service;
  private final String procedureCall;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    service.appeler(procedureCall);
    return RepeatStatus.FINISHED;
  }
}
