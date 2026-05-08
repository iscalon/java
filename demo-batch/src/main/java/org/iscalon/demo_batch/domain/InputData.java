package org.iscalon.demo_batch.domain;

import java.math.BigDecimal;

public record InputData(Long vRef, Long userId, BigDecimal amount, String status) {}
