package org.gft.gbt.service;

import org.gft.gbt.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailNotificationService.class);

    @Override
    public void notify(Customer customer, String message) {
        LOGGER.info("Sending email to {}: {}", customer.getId(), message);
    }
}
