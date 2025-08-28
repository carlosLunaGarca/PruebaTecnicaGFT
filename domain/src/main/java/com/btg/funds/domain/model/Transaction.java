package com.btg.funds.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/** Transaction related to a fund subscription. */
public record Transaction(String userId, String id, String type, String fundId, BigDecimal amount, Instant at) {}
