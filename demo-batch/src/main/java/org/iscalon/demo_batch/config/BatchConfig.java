package org.iscalon.demo_batch.config;

import org.iscalon.demo_batch.domain.UserWorkUnit;
import org.iscalon.demo_batch.out.repository.CalculRepository;
import org.iscalon.demo_batch.out.storedprocedure.PourAppelerStoredProcedure;
import org.iscalon.demo_batch.partition.UserPartitioner;
import org.iscalon.demo_batch.reader.UserDocumentsPagingReader;
import org.iscalon.demo_batch.reader.UserLoadingStrategy;
import org.iscalon.demo_batch.tasklet.StoredProcedureTasklet;
import org.iscalon.demo_batch.writer.CalculationWriter;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
class BatchConfig {

  private static final int PARTITION_COUNT = 10;

  /** Nombre maximum d'utilisateurs traités par 1 thread */
  private static final int CHUNK_SIZE = 500;

  @Bean
  Job userCalculationJob(
      JobRepository jobRepository,
      SingleExecutionJobExecutionListener listener,
      Step prepareInputDataStep,
      Step enrichInputDataStep,
      Step partitionedUserCalculationStep) {
    return new JobBuilder("userCalculationJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .listener(listener)
        .start(prepareInputDataStep)
        .next(enrichInputDataStep)
        .next(partitionedUserCalculationStep)
        .build();
  }

  @Bean
  Step prepareInputDataStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      PourAppelerStoredProcedure service) {
    return new StepBuilder("prepareInputDataStep", jobRepository)
        .tasklet(new StoredProcedureTasklet(service, "PROC_INIT_1"), transactionManager)
        .build();
  }

  @Bean
  Step enrichInputDataStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      PourAppelerStoredProcedure service) {
    return new StepBuilder("enrichInputDataStep", jobRepository)
        .tasklet(new StoredProcedureTasklet(service, "PROC_INIT_2"), transactionManager)
        .build();
  }

  @Bean
  Step partitionedUserCalculationStep(
      JobRepository jobRepository,
      Step userCalculationWorkerStep,
      Partitioner userPartitioner,
      TaskExecutor batchTaskExecutor) {
    return new StepBuilder("partitionedUserCalculationStep", jobRepository)
        .partitioner("userCalculationWorkerStep", userPartitioner)
        .step(userCalculationWorkerStep)
        .gridSize(PARTITION_COUNT)
        .taskExecutor(batchTaskExecutor)
        .build();
  }

  @Bean
  Step userCalculationWorkerStep(
      JobRepository jobRepository,
      @Qualifier("userDocumentsReader") ItemReader<UserWorkUnit> userDocumentsReader,
      CalculationWriter calculationWriter) {
    return new StepBuilder("userCalculationWorkerStep", jobRepository)
        .<UserWorkUnit, UserWorkUnit>chunk(CHUNK_SIZE)
        .reader(userDocumentsReader)
        .writer(calculationWriter)
        .build();
  }

  @Bean
  Partitioner userPartitioner() {
    return new UserPartitioner(PARTITION_COUNT);
  }

  @Bean(name = "userDocumentsReader")
  @StepScope
  ItemReader<UserWorkUnit> userDocumentsReader(
      UserLoadingStrategy users,
      @Value("#{stepExecutionContext['bucket']}") Integer bucket,
      @Value("#{stepExecutionContext['bucketCount']}") Integer bucketCount) {
    return new UserDocumentsPagingReader(users, bucket, bucketCount, CHUNK_SIZE);
  }

  @Bean
  CalculationWriter calculationWriter(CalculRepository repository) {
    return new CalculationWriter(repository);
  }

  @Bean
  TaskExecutor batchTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(PARTITION_COUNT);
    executor.setMaxPoolSize(PARTITION_COUNT);
    executor.setQueueCapacity(0);
    executor.setThreadNamePrefix("batch-user-");
    executor.initialize();
    return executor;
  }
}
