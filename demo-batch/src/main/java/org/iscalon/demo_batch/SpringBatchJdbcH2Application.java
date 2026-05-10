package org.iscalon.demo_batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
public class SpringBatchJdbcH2Application {

  public static void main(String[] args) {
    log.info("Spring BatchJdbcH2Application started");
    ConfigurableApplicationContext context =
        SpringApplication.run(SpringBatchJdbcH2Application.class, args);
    log.info("Spring BatchJdbcH2Application finished");
    int exitCode = SpringApplication.exit(context);
    System.exit(exitCode);
  }
}
