package com.btg.funds.domain.model;

/**
 * Represents a fund in the catalog.
 */
public record Fund(String id, String name, double minAmount, String category) {}
