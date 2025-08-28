package com.btg.funds.infra.id;

import com.btg.funds.domain.spi.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Simple ULID/UUID generator.
 */
@Component
public class UlidGenerator implements IdGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
