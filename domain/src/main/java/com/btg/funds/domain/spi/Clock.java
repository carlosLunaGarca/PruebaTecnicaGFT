package com.btg.funds.domain.spi;

import java.time.Instant;

/** Port for obtaining time information. */
public interface Clock {
    Instant now();
}
