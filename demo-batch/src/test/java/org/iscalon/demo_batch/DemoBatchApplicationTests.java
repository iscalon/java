package org.iscalon.demo_batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.iscalon.demo_batch.annotation.BatchIntegrationTest;
import org.iscalon.demo_batch.domain.CalculatedResult;
import org.iscalon.demo_batch.domain.UserWorkUnit;
import org.iscalon.demo_batch.out.repository.CalculRepository;
import org.iscalon.demo_batch.out.storedprocedure.PourAppelerStoredProcedure;
import org.iscalon.demo_batch.reader.UserLoadingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@ExtendWith(MockitoExtension.class)
@BatchIntegrationTest
class DemoBatchApplicationTests {

  @Autowired private JobOperatorTestUtils jobTestUtils;

  @MockitoBean private PourAppelerStoredProcedure storedProcedures;
  @MockitoBean private CalculRepository calculs;

  @MockitoBean private UserLoadingStrategy users;

  @Captor private ArgumentCaptor<List<CalculatedResult>> calculsCaptor;

  @BeforeEach
  void setUp() {
    doNothing().when(storedProcedures).appeler(anyString());
    doNothing().when(calculs).batchInsertResults(anyList());
    doReturn("TEST_LOADER").when(users).key();
  }

  @Test
  void should_run_job() throws Exception {
    doAnswer(
            invocation ->
                prepareResponseFor(
                    invocation.getArgument(0),
                    invocation.getArgument(3)))
        .when(users)
        .load(anyInt(), anyInt(), anyInt(), any());

    JobParameters params =
        new JobParametersBuilder().addString("date", "2026-12-10").toJobParameters();

    JobExecution execution = jobTestUtils.startJob(params);

    assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    assertBatchResults();
  }

  private void assertBatchResults() {
    InOrder inOrder = inOrder(storedProcedures, calculs);
    inOrder.verify(storedProcedures).appeler("PROC_INIT_1");
    inOrder.verify(storedProcedures).appeler("PROC_INIT_2");
    // 2 appels d'insertions en masse car les 2 utilisateurs vont être répartis sur 2 buckets
    // différents (v. méthode 'prepareResponseFor' plus bas).
    // Les 2 résultats calculés pour l'utilisateur 1 seront écrits d'un coup
    // Les 3 résultats calculés pour l'utilisateur 2 seront écrits d'un coup
    inOrder.verify(calculs, times(2)).batchInsertResults(calculsCaptor.capture());
    List<List<CalculatedResult>> captorAllValues = calculsCaptor.getAllValues();

    assertThat(captorAllValues).extracting(List::size).containsExactlyInAnyOrder(2, 3);

    List<CalculatedResult> allResults = captorAllValues.stream().flatMap(List::stream).toList();
    assertThat(allResults)
        .hasSize(5)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields("amount")
        .containsExactlyInAnyOrder(
            // 2 résultats pour l'utilisateur 1
            new CalculatedResult("Y0001", 1L, null),
            new CalculatedResult("Y0001", 2L, null),
            // 3 résultats pour l'utilisateur 2
            new CalculatedResult("Y0002", 3L, null),
            new CalculatedResult("Y0002", 4L, null),
            new CalculatedResult("Y0002", 5L, null));
  }

  private List<UserWorkUnit> prepareResponseFor(int bucket, String lastUserId) {
    if (lastUserId != null) {
      return List.of();
    }
    if (bucket == 1) {
      return List.of(new UserWorkUnit("Y0001", Set.of(1L, 2L)));
    }
    if (bucket == 2) {
      return List.of(new UserWorkUnit("Y0002", Set.of(3L, 4L, 5L)));
    }
    return List.of();
  }
}
