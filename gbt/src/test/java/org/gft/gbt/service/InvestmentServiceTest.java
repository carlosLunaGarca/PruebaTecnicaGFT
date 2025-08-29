package org.gft.gbt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import org.gft.gbt.exception.FundNotFoundException;
import org.gft.gbt.exception.InsufficientBalanceException;
import org.gft.gbt.exception.SubscriptionNotFoundException;
import org.gft.gbt.model.*;
import org.gft.gbt.repository.CustomerRepository;
import org.gft.gbt.repository.FundRepository;
import org.gft.gbt.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class InvestmentServiceTest {

    @Mock
    private FundRepository fundRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private NotificationServiceFactory notificationFactory;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private InvestmentService investmentService;

    private Customer testCustomer;
    private Fund testFund;
    private final String customerId = "test-customer-1";
    private final Integer fundId = 1;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer(
            customerId, 
            new BigDecimal("1000000"), 
            NotificationPreference.EMAIL
        );
        
        testFund = new Fund(
            fundId, 
            "Test Fund", 
            new BigDecimal("100000"), 
            "FIC"
        );
        
        when(notificationFactory.getService(any(NotificationPreference.class)))
            .thenReturn(notificationService);
    }

    @Test
    void subscribe_ShouldDeductBalanceAndCreateSubscription() {
        // Arrange
        when(customerRepository.findById(customerId))
            .thenReturn(Optional.of(testCustomer));
        when(fundRepository.findById(fundId))
            .thenReturn(Optional.of(testFund));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        investmentService.subscribe(customerId, fundId);

        // Assert
        verify(customerRepository).save(argThat(customer -> 
            customer.getBalance().equals(new BigDecimal("900000")) &&
            customer.getSubscriptions().contains(fundId)
        ));
        
        verify(transactionRepository).save(argThat(transaction ->
            transaction.getCustomerId().equals(customerId) &&
            transaction.getFundId().equals(fundId) &&
            transaction.getType() == TransactionType.SUBSCRIPTION &&
            transaction.getAmount().equals(testFund.getMinimumAmount())
        ));
        
        verify(notificationService).notify(eq(testCustomer), contains("Subscribed to fund"));
    }

    @Test
    void subscribe_WhenInsufficientBalance_ShouldThrowException() {
        // Arrange
        testCustomer.setBalance(new BigDecimal("50000")); // Less than fund minimum
        when(customerRepository.findById(customerId))
            .thenReturn(Optional.of(testCustomer));
        when(fundRepository.findById(fundId))
            .thenReturn(Optional.of(testFund));

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () -> 
            investmentService.subscribe(customerId, fundId)
        );
        
        verify(customerRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void cancel_ShouldRefundAndRemoveSubscription() {
        // Arrange
        testCustomer.getSubscriptions().add(fundId);
        when(customerRepository.findById(customerId))
            .thenReturn(Optional.of(testCustomer));
        when(fundRepository.findById(fundId))
            .thenReturn(Optional.of(testFund));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        investmentService.cancel(customerId, fundId);

        // Assert
        verify(customerRepository).save(argThat(customer -> 
            customer.getBalance().equals(new BigDecimal("1100000")) &&
            !customer.getSubscriptions().contains(fundId)
        ));
        
        verify(transactionRepository).save(argThat(transaction ->
            transaction.getType() == TransactionType.CANCELLATION
        ));
        
        verify(notificationService).notify(eq(testCustomer), contains("Cancelled subscription"));
    }

    @Test
    void cancel_WhenNoSubscription_ShouldThrowException() {
        // Arrange
        when(customerRepository.findById(customerId))
            .thenReturn(Optional.of(testCustomer));
        when(fundRepository.findById(fundId))
            .thenReturn(Optional.of(testFund));

        // Act & Assert
        assertThrows(SubscriptionNotFoundException.class, () -> 
            investmentService.cancel(customerId, fundId)
        );
        
        verify(customerRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransactions_ShouldReturnAllTransactionsForCustomer() {
        // Arrange
        List<Transaction> expectedTransactions = Arrays.asList(
            new Transaction("tx1", customerId, 1, TransactionType.SUBSCRIPTION, 
                          new BigDecimal("100000"), LocalDateTime.now()),
            new Transaction("tx2", customerId, 1, TransactionType.CANCELLATION, 
                          new BigDecimal("100000"), LocalDateTime.now())
        );
        
        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId))
            .thenReturn(expectedTransactions);

        // Act
        List<Transaction> result = investmentService.getTransactions(customerId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("tx1", result.get(0).getId());
        assertEquals(TransactionType.SUBSCRIPTION, result.get(0).getType());
    }

    @Test
    void notify_ShouldUseCorrectNotificationService() {
        // Arrange
        testCustomer.setNotificationPreference(NotificationPreference.EMAIL);
        when(notificationFactory.getService(NotificationPreference.EMAIL))
            .thenReturn(notificationService);

        // Act
        investmentService = new InvestmentService(
            fundRepository, customerRepository, 
            transactionRepository, notificationFactory
        );
        
        // This is a bit of a hack to test the private method
        // In a real scenario, you might want to test this through public methods
        // or refactor to make it testable
        investmentService.subscribe(customerId, fundId);

        // Assert
        verify(notificationService).notify(any(), anyString());
    }
}
