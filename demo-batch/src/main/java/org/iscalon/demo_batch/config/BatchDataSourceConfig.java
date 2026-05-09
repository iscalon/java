package org.iscalon.demo_batch.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class BatchDataSourceConfig {

  /** Datasource Spring Batch (H2 travail) */
  @Bean
  @ConfigurationProperties("spring.datasource.travail-batch")
  public DataSourceProperties batchDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @BatchDataSource
  public DataSource batchDataSource(
      @Qualifier("batchDataSourceProperties") DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().build();
  }

  /** JdbcTemplate vers la BDD métier (utilise automatiquement spring.datasource.*) */
  @Bean
  public JdbcTemplate sourceJdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }

  @Bean
  public NamedParameterJdbcTemplate sourceNamedParameterJdbcTemplate(DataSource dataSource) {
    return new NamedParameterJdbcTemplate(dataSource);
  }

  /** JdbcTemplate H2 batch */
  @Bean
  public JdbcTemplate batchJdbcTemplate(@Qualifier("batchDataSource") DataSource batchDataSource) {
    return new JdbcTemplate(batchDataSource);
  }

  @Bean
  public NamedParameterJdbcTemplate batchNamedParameterJdbcTemplate(
      @Qualifier("batchDataSource") DataSource batchDataSource) {
    return new NamedParameterJdbcTemplate(batchDataSource);
  }
}
