package org.gft.gbt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "customers")
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    private String id;
    private BigDecimal balance;
    private NotificationPreference notificationPreference;
    private Set<Integer> subscriptions = new HashSet<>();

    public Customer(String id, BigDecimal balance, NotificationPreference notificationPreference) {
        this.id = id;
        this.balance = balance;
        this.notificationPreference = notificationPreference;
    }
}

