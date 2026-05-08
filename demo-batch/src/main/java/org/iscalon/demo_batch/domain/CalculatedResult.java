package org.iscalon.demo_batch.domain;

import java.math.BigDecimal;

public record CalculatedResult(String userId, Long inputDataId, BigDecimal amount) {}
