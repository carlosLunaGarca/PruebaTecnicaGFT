package com.btg.funds.infra.time;

import com.btg.funds.domain.spi.Clock;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * System clock implementation.
 */
@Component
public class SystemClock implements Clock {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
