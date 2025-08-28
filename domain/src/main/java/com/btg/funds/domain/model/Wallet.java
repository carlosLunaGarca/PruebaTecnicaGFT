package com.btg.funds.domain.model;

import java.math.BigDecimal;

/** Wallet associated to a user. */
public record Wallet(String userId, BigDecimal balance, UserPrefs prefs) {}
