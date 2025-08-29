package org.gft.gbt.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.gft.gbt.exception.FundNotFoundException;
import org.gft.gbt.exception.InsufficientBalanceException;
import org.gft.gbt.exception.SubscriptionNotFoundException;
import org.gft.gbt.model.Customer;
import org.gft.gbt.model.Fund;
import org.gft.gbt.model.NotificationPreference;
import org.gft.gbt.model.Transaction;
import org.gft.gbt.model.TransactionType;
import org.gft.gbt.repository.CustomerRepository;
import org.gft.gbt.repository.FundRepository;
import org.gft.gbt.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvestmentService {
    private final FundRepository fundRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationServiceFactory notificationFactory;

    public InvestmentService(FundRepository fundRepository, CustomerRepository customerRepository,
                             TransactionRepository transactionRepository, NotificationServiceFactory notificationFactory) {
        this.fundRepository = fundRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.notificationFactory = notificationFactory;
    }

    @Transactional
    public void subscribe(String customerId, Integer fundId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new FundNotFoundException("Fund not found"));

        // Validación para suscripción duplicada
        if (customer.getSubscriptions().contains(fundId)) {
            throw new RuntimeException("Customer is already subscribed to this fund");
        }

        BigDecimal min = fund.getMinimumAmount();
        if (customer.getBalance().compareTo(min) < 0) {
            throw new InsufficientBalanceException(
                    "No tiene saldo disponible para vincularse al fondo " + fund.getName());
        }
        customer.setBalance(customer.getBalance().subtract(min));
        customer.getSubscriptions().add(fundId);
        customerRepository.save(customer);

        Transaction tx = new Transaction(UUID.randomUUID().toString(), customerId, fundId,
                TransactionType.SUBSCRIPTION, min, LocalDateTime.now());
        transactionRepository.save(tx);

        notify(customer, "Subscribed to fund " + fund.getName());
    }

    @Transactional
    public void cancel(String customerId, Integer fundId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new FundNotFoundException("Fund not found"));

        if (!customer.getSubscriptions().remove(fundId)) {
            throw new SubscriptionNotFoundException("Subscription not found");
        }
        BigDecimal amount = fund.getMinimumAmount();
        customer.setBalance(customer.getBalance().add(amount));
        customerRepository.save(customer);

        Transaction tx = new Transaction(UUID.randomUUID().toString(), customerId, fundId,
                TransactionType.CANCELLATION, amount, LocalDateTime.now());
        transactionRepository.save(tx);

        notify(customer, "Cancelled subscription to fund " + fund.getName());
    }

    public java.util.List<Transaction> getTransactions(String customerId) {
        return transactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    private void notify(Customer customer, String message) {
        NotificationPreference preference = customer.getNotificationPreference();
        notificationFactory.getService(preference).notify(customer, message);
    }
}
