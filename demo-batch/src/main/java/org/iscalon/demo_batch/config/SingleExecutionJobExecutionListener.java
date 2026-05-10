package org.iscalon.demo_batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SingleExecutionJobExecutionListener implements JobExecutionListener {

  private final JobRepository jobRepository;

  @Override
  public void beforeJob(JobExecution jobExecution) {
    String jobName = jobExecution.getJobInstance().getJobName();
    long runningCount =
        jobRepository.findRunningJobExecutions(jobName).stream()
            .filter(other -> other.getId() != jobExecution.getId())
            .count();

    if (runningCount > 0) {
      throw new IllegalStateException("Le job est déjà en cours.");
    }
  }
}
