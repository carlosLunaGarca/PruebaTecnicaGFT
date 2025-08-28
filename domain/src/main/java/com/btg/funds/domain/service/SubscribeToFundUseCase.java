package com.btg.funds.domain.service;

import java.math.BigDecimal;

/** Use case for subscribing a user to a fund. */
public interface SubscribeToFundUseCase {
    void subscribe(String userId, String fundId, BigDecimal amount);
}
