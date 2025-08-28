package org.gft.gbt.service;

import org.gft.gbt.model.NotificationPreference;
import org.springframework.stereotype.Component;

@Component
public class NotificationServiceFactory {
    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;

    public NotificationServiceFactory(EmailNotificationService emailService, SmsNotificationService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public NotificationService getService(NotificationPreference preference) {
        return preference == NotificationPreference.SMS ? smsService : emailService;
    }
}
