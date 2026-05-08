package org.iscalon.demo_batch.config;

import javax.sql.DataSource;
import org.iscalon.demo_batch.domain.CalculatedResult;
import org.iscalon.demo_batch.domain.InputData;
import org.iscalon.demo_batch.partition.UserPartitioner;
import org.iscalon.demo_batch.processor.UserCalculationProcessor;
import org.iscalon.demo_batch.tasklet.StoredProcedureTasklet;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

  private static final int CHUNK_SIZE = 100;

  @Bean
  public Job userCalculationJob(
      JobRepository jobRepository,
      Step prepareInputDataStep,
      Step enrichInputDataStep,
      Step partitionedUserCalculationStep) {
    return new JobBuilder("userCalculationJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(prepareInputDataStep)
        .next(enrichInputDataStep)
        .next(partitionedUserCalculationStep)
        .build();
  }

  @Bean
  public Step prepareInputDataStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      JdbcTemplate jdbcTemplate) {
    return new StepBuilder("prepareInputDataStep", jobRepository)
        .tasklet(new StoredProcedureTasklet(jdbcTemplate, "CALL PROC_INIT_1()"), transactionManager)
        .build();
  }

  @Bean
  public Step enrichInputDataStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      JdbcTemplate jdbcTemplate) {
    return new StepBuilder("enrichInputDataStep", jobRepository)
        .tasklet(new StoredProcedureTasklet(jdbcTemplate, "CALL PROC_INIT_2()"), transactionManager)
        .build();
  }

  @Bean
  public Step partitionedUserCalculationStep(
      JobRepository jobRepository,
      Step userCalculationWorkerStep,
      Partitioner userPartitioner,
      TaskExecutor batchTaskExecutor) {
    return new StepBuilder("partitionedUserCalculationStep", jobRepository)
        .partitioner("userCalculationWorkerStep", userPartitioner)
        .step(userCalculationWorkerStep)
        .gridSize(4)
        .taskExecutor(batchTaskExecutor)
        .build();
  }

  @Bean
  public Step userCalculationWorkerStep(
      JobRepository jobRepository,
      JdbcCursorItemReader<InputData> inputDataReader,
      ItemProcessor<InputData, CalculatedResult> userCalculationProcessor,
      JdbcBatchItemWriter<CalculatedResult> calculatedResultWriter) {
    return new StepBuilder("userCalculationWorkerStep", jobRepository)
        .<InputData, CalculatedResult>chunk(CHUNK_SIZE)
        .reader(inputDataReader)
        .processor(userCalculationProcessor)
        .writer(calculatedResultWriter)
        .build();
  }

  @Bean
  public Partitioner userPartitioner(JdbcTemplate jdbcTemplate) {
    return new UserPartitioner(jdbcTemplate);
  }

  @Bean
  @StepScope
  public JdbcCursorItemReader<InputData> inputDataReader(
      DataSource dataSource, @Value("#{stepExecutionContext['userId']}") Long userId) {
    return new JdbcCursorItemReaderBuilder<InputData>()
        .name("inputDataReader-user-" + userId)
        .dataSource(dataSource)
        .sql(
            """
                        SELECT v_ref, amount, status
                        FROM source_data
                        WHERE user_id = ?
                        ORDER BY v_ref
                        """)
        .preparedStatementSetter(ps -> ps.setLong(1, userId))
        .rowMapper(
            (rs, rowNum) ->
                new InputData(
                    rs.getLong("v_ref"),
                    userId,
                    rs.getBigDecimal("amount"),
                    rs.getString("status")))
        .build();
  }

  @Bean
  public ItemProcessor<InputData, CalculatedResult> userCalculationProcessor() {
    return new UserCalculationProcessor();
  }

  @Bean
  public JdbcBatchItemWriter<CalculatedResult> calculatedResultWriter(DataSource dataSource) {
    return new JdbcBatchItemWriterBuilder<CalculatedResult>()
        .dataSource(dataSource)
        .sql(
            """
                        INSERT INTO calculated_result
                            (v_ref, user_id, calculated_amount)
                        VALUES
                            (:inputDataId, :userId, :amount)
                        """)
        .beanMapped()
        .build();
  }

  @Bean
  public TaskExecutor batchTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(20);
    executor.setThreadNamePrefix("batch-user-");
    executor.initialize();
    return executor;
  }
}
