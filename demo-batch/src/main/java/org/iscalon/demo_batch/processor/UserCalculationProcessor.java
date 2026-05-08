package org.iscalon.demo_batch.processor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import org.iscalon.demo_batch.domain.CalculatedResult;
import org.iscalon.demo_batch.domain.InputData;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class UserCalculationProcessor implements ItemProcessor<InputData, CalculatedResult> {

  @Override
  public CalculatedResult process(InputData item) {
    BigDecimal coefficient =
        Optional.of(item)
            .filter(i -> Objects.equals(i.status(), "PENDING"))
            .map(_ -> BigDecimal.valueOf(1.10))
            .orElse(BigDecimal.ONE);

    BigDecimal calculatedAmount =
        Optional.of(item)
            .map(InputData::amount)
            .map(amount -> amount.multiply(coefficient).setScale(2, RoundingMode.HALF_UP))
            .orElseThrow();

    return new CalculatedResult(item.userId(), item.vRef(), calculatedAmount);
  }
}
