package org.gft.gbt.service;

import org.gft.gbt.model.Customer;

public interface NotificationService {
    void notify(Customer customer, String message);
}
