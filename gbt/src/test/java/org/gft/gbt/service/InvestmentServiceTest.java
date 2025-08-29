package org.gft.gbt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
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

@ExtendWith(MockitoExtension.class)
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
    private final BigDecimal fundMinimumAmount = new BigDecimal("100000");

    @BeforeEach
    void setUp() {
        testCustomer = new Customer(customerId, new BigDecimal("500000"), NotificationPreference.EMAIL);
        testFund = new Fund(fundId, "Test Fund", fundMinimumAmount, "FIC");
        // Remover el stub innecesario
    }
    

    @Test
    void subscribe_ShouldSucceed_WhenCustomerHasSufficientBalance() {
        // Arrange
        when(notificationFactory.getService(any(NotificationPreference.class)))
            .thenReturn(notificationService);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(fundRepository.findById(fundId)).thenReturn(Optional.of(testFund));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        investmentService.subscribe(customerId, fundId);

        // Assert
        verify(customerRepository).save(argThat(customer -> 
            customer.getBalance().equals(new BigDecimal("400000")) &&
            customer.getSubscriptions().contains(fundId)
        ));
        
        verify(transactionRepository).save(argThat(transaction ->
            transaction.getCustomerId().equals(customerId) &&
            transaction.getFundId().equals(fundId) &&
            transaction.getType() == TransactionType.SUBSCRIPTION &&
            transaction.getAmount().equals(fundMinimumAmount)
        ));
        
        verify(notificationService).notify(eq(testCustomer), contains("Subscribed to fund"));
    }

    @Test
    void subscribe_ShouldFail_WhenCustomerNotFound() {
        // Arrange
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            investmentService.subscribe(customerId, fundId)
        );
        
        assertTrue(exception.getMessage().contains("Customer not found"));
        verify(customerRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void subscribe_ShouldFail_WhenFundNotFound() {
        // Arrange
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(fundRepository.findById(fundId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(FundNotFoundException.class, () -> 
            investmentService.subscribe(customerId, fundId)
        );
        
        verify(customerRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void subscribe_ShouldFail_WhenInsufficientBalance() {
        // Arrange
        testCustomer.setBalance(new BigDecimal("50000")); // Less than fund minimum
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(fundRepository.findById(fundId)).thenReturn(Optional.of(testFund));

        // Act & Assert
        assertThrows(InsufficientBalanceException.class, () -> 
            investmentService.subscribe(customerId, fundId)
        );
        
        verify(customerRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
        verify(notificationService, never()).notify(any(), any());
    }

    @Test
    void subscribe_ShouldFail_WhenAlreadySubscribed() {
        // Arrange
        testCustomer.getSubscriptions().add(fundId); // Already subscribed
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(fundRepository.findById(fundId)).thenReturn(Optional.of(testFund));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            investmentService.subscribe(customerId, fundId)
        );
        
        assertTrue(exception.getMessage().contains("already subscribed"));
        verify(customerRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void cancel_ShouldSucceed_WhenSubscriptionExists() {
        // Arrange
        testCustomer.getSubscriptions().add(fundId);
        when(notificationFactory.getService(any(NotificationPreference.class)))
            .thenReturn(notificationService);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(fundRepository.findById(fundId)).thenReturn(Optional.of(testFund));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        investmentService.cancel(customerId, fundId);

        // Assert
        verify(customerRepository).save(argThat(customer -> 
            customer.getBalance().equals(new BigDecimal("600000")) &&
            !customer.getSubscriptions().contains(fundId)
        ));
        
        verify(transactionRepository).save(argThat(transaction ->
            transaction.getType() == TransactionType.CANCELLATION &&
            transaction.getAmount().equals(fundMinimumAmount)
        ));
        
        verify(notificationService).notify(eq(testCustomer), contains("Cancelled subscription"));
    }

    @Test
    void cancel_ShouldFail_WhenCustomerNotFound() {
        // Arrange
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            investmentService.cancel(customerId, fundId)
        );
        
        assertTrue(exception.getMessage().contains("Customer not found"));
        verify(customerRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void cancel_ShouldFail_WhenFundNotFound() {
        // Arrange
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(fundRepository.findById(fundId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(FundNotFoundException.class, () -> 
            investmentService.cancel(customerId, fundId)
        );
        
        verify(customerRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void cancel_ShouldFail_WhenSubscriptionDoesNotExist() {
        // Arrange
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(fundRepository.findById(fundId)).thenReturn(Optional.of(testFund));

        // Act & Assert
        assertThrows(SubscriptionNotFoundException.class, () -> 
            investmentService.cancel(customerId, fundId)
        );
        
        verify(customerRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
        verify(notificationService, never()).notify(any(), any());
    }

    @Test
    void getTransactions_ShouldReturnTransactionsForCustomer() {
        // Arrange
        List<Transaction> expectedTransactions = Arrays.asList(
            new Transaction("tx1", customerId, fundId, TransactionType.SUBSCRIPTION, 
                          fundMinimumAmount, LocalDateTime.now().minusDays(1)),
            new Transaction("tx2", customerId, fundId, TransactionType.CANCELLATION, 
                          fundMinimumAmount, LocalDateTime.now())
        );
        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId))
            .thenReturn(expectedTransactions);

        // Act
        List<Transaction> result = investmentService.getTransactions(customerId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("tx1", result.get(0).getId());
        assertEquals(TransactionType.SUBSCRIPTION, result.get(0).getType());
        assertEquals("tx2", result.get(1).getId());
        assertEquals(TransactionType.CANCELLATION, result.get(1).getType());
        verify(transactionRepository, times(1)).findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Test
    void getTransactions_ShouldReturnEmptyList_WhenNoTransactions() {
        // Arrange
        when(transactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId))
            .thenReturn(Collections.emptyList());

        // Act
        List<Transaction> result = investmentService.getTransactions(customerId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1)).findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Test
    void subscribe_ShouldUseCorrectNotificationService() {
        // Arrange
        testCustomer.setNotificationPreference(NotificationPreference.SMS);
        NotificationService smsService = mock(NotificationService.class);
        
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(fundRepository.findById(fundId)).thenReturn(Optional.of(testFund));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationFactory.getService(NotificationPreference.SMS)).thenReturn(smsService);

        // Act
        investmentService.subscribe(customerId, fundId);

        // Assert
        verify(notificationFactory).getService(NotificationPreference.SMS);
        verify(smsService).notify(eq(testCustomer), anyString());
        verify(notificationService, never()).notify(any(), any()); // The original mock should not be called
    }
}
