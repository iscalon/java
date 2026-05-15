package org.iscalon.demo_batch.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.batch.autoconfigure.BatchTransactionManager;
import org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
class BatchDataSourceConfig {

  @Bean
  @ConfigurationProperties("spring.datasource")
  DataSourceProperties dataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @Primary
  DataSource dataSource() {
    return dataSourceProperties().initializeDataSourceBuilder().build();
  }

  @Bean
  @Primary
  JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(dataSource());
  }

  @Bean
  @Primary
  NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
    return new NamedParameterJdbcTemplate(dataSource());
  }

  @Bean
  @Primary
  PlatformTransactionManager transactionManager() {
    return new DataSourceTransactionManager(dataSource());
  }

  /** Datasource Spring Batch (H2 travail) */
  @Bean
  @ConfigurationProperties("spring.datasource.travail-batch")
  DataSourceProperties batchDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name = "batchDataSource")
  @BatchDataSource
  DataSource batchDataSource(
      @Qualifier("batchDataSourceProperties") DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().build();
  }

  @Bean(name = "batchJdbcTemplate")
  JdbcTemplate batchJdbcTemplate(@Qualifier("batchDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean(name = "batchNamedParameterJdbcTemplate")
  NamedParameterJdbcTemplate batchNamedParameterJdbcTemplate(
      @Qualifier("batchDataSource") DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }

  @Bean(name = "batchTransactionManager")
  @BatchTransactionManager
  PlatformTransactionManager batchTransactionManager(
      @Qualifier("batchDataSource") DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }
}
