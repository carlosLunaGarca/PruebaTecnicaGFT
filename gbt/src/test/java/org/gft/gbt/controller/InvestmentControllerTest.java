package org.gft.gbt.controller;

import org.gft.gbt.model.Transaction;
import org.gft.gbt.model.TransactionType;
import org.gft.gbt.service.InvestmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentControllerTest {

    @Mock
    private InvestmentService investmentService;

    @InjectMocks
    private InvestmentController investmentController;

    private final String customerId = "test-customer-1";
    private final Integer fundId = 1;

    @Test
    void subscribe_ShouldReturnCreatedStatus() {
        // Arrange
        doNothing().when(investmentService).subscribe(anyString(), anyInt());

        // Act
        ResponseEntity<Void> response = investmentController.subscribe(customerId, fundId);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(investmentService, times(1)).subscribe(eq(customerId), eq(fundId));
    }

    @Test
    void cancel_ShouldReturnNoContentStatus() {
        // Arrange
        doNothing().when(investmentService).cancel(anyString(), anyInt());

        // Act
        ResponseEntity<Void> response = investmentController.cancel(customerId, fundId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(investmentService, times(1)).cancel(eq(customerId), eq(fundId));
    }

    @Test
    void getTransactions_ShouldReturnTransactionList() {
        // Arrange
        List<Transaction> expectedTransactions = Arrays.asList(
            new Transaction(
                UUID.randomUUID().toString(),
                customerId,
                fundId,
                TransactionType.SUBSCRIPTION,
                new BigDecimal("100000"),
                LocalDateTime.now()
            ),
            new Transaction(
                UUID.randomUUID().toString(),
                customerId,
                fundId,
                TransactionType.CANCELLATION,
                new BigDecimal("100000"),
                LocalDateTime.now()
            )
        );
        
        when(investmentService.getTransactions(customerId)).thenReturn(expectedTransactions);

        // Act
        List<Transaction> result = investmentController.transactions(customerId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TransactionType.SUBSCRIPTION, result.get(0).getType());
        verify(investmentService, times(1)).getTransactions(eq(customerId));
    }

    @Test
    void subscribe_WhenServiceThrowsException_ShouldPropagate() {
        // Arrange
        doThrow(new RuntimeException("Service error"))
            .when(investmentService).subscribe(anyString(), anyInt());

        // Act & Assert
        assertThrows(
            RuntimeException.class,
            () -> investmentController.subscribe(customerId, fundId)
        );
    }

    @Test
    void cancel_WhenServiceThrowsException_ShouldPropagate() {
        // Arrange
        doThrow(new RuntimeException("Service error"))
            .when(investmentService).cancel(anyString(), anyInt());

        // Act & Assert
        assertThrows(
            RuntimeException.class,
            () -> investmentController.cancel(customerId, fundId)
        );
    }

    @Test
    void getTransactions_WhenServiceThrowsException_ShouldPropagate() {
        // Arrange
        when(investmentService.getTransactions(anyString()))
            .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(
            RuntimeException.class,
            () -> investmentController.transactions(customerId)
        );
    }
}
