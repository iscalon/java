package org.iscalon.demo_batch.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.TestInstance;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBatchTest
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public @interface BatchIntegrationTest {

  @AliasFor(annotation = SpringBootTest.class, attribute = "properties")
  String[] properties() default {"spring.batch.job.enabled=false"};
}
