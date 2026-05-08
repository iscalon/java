package org.iscalon.demo_batch.domain;

import java.math.BigDecimal;

public record CalculatedResult(Long userId, Long inputDataId, BigDecimal amount) {}
