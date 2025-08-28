package com.btg.funds.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/** Subscription of a user to a fund. */
public record Subscription(String userId, String fundId, BigDecimal amount, String status, Instant createdAt) {}
