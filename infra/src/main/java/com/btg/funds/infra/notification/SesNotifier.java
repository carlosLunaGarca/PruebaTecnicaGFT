package com.btg.funds.infra.notification;

import com.btg.funds.domain.spi.NotificationPort;
import org.springframework.stereotype.Component;

/**
 * Notification adapter using Amazon SES.
 */
@Component
public class SesNotifier implements NotificationPort {
    @Override
    public void notify(String userId, String message) {
        // TODO: integrate with SES
    }
}
