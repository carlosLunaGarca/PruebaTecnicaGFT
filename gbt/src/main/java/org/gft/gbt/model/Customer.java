package org.gft.gbt.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "customers")
public class Customer {
    @Id
    private String id;
    private BigDecimal balance;
    private NotificationPreference notificationPreference;
    private Set<Integer> subscriptions = new HashSet<>();

    public Customer() {
    }

    public Customer(String id, BigDecimal balance, NotificationPreference notificationPreference) {
        this.id = id;
        this.balance = balance;
        this.notificationPreference = notificationPreference;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public NotificationPreference getNotificationPreference() {
        return notificationPreference;
    }

    public void setNotificationPreference(NotificationPreference notificationPreference) {
        this.notificationPreference = notificationPreference;
    }

    public Set<Integer> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Integer> subscriptions) {
        this.subscriptions = subscriptions;
    }
}

