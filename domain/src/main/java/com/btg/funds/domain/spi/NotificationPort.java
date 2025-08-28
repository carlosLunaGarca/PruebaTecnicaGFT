package com.btg.funds.domain.spi;

/** Port for sending notifications. */
public interface NotificationPort {
    void notify(String userId, String message);
}
