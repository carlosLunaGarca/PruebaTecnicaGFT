package org.gft.gbt.controller;

import org.gft.gbt.model.Customer;
import org.gft.gbt.model.NotificationPreference;
import org.gft.gbt.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AdminController adminController;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer(
            "test-customer-1",
            new BigDecimal("1000000"),
            NotificationPreference.EMAIL
        );
    }

    @Test
    void listCustomers_ShouldReturnAllCustomers() {
        // Arrange
        List<Customer> expectedCustomers = Arrays.asList(
            testCustomer,
            new Customer("test-customer-2", new BigDecimal("500000"), 
                        NotificationPreference.SMS)
        );
        when(customerRepository.findAll()).thenReturn(expectedCustomers);

        // Act
        List<Customer> result = adminController.listCustomers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("test-customer-1", result.get(0).getId());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void createCustomer_WithNewCustomer_ShouldReturnCreatedCustomer() {
        // Arrange
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // Act
        Customer result = adminController.createCustomer(testCustomer);

        // Assert
        assertNotNull(result);
        assertEquals("test-customer-1", result.getId());
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    void createCustomer_WithExistingId_ShouldThrowException() {
        // Arrange
        when(customerRepository.existsById(testCustomer.getId())).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> adminController.createCustomer(testCustomer)
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("400 BAD_REQUEST \"Ya existe un cliente con este ID\"", 
                    exception.getMessage());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void createCustomer_WithDuplicateKey_ShouldThrowException() {
        // Arrange
        when(customerRepository.save(any(Customer.class)))
            .thenThrow(new DuplicateKeyException("Duplicate key"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> adminController.createCustomer(testCustomer)
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(customerRepository, times(1)).save(any());
    }

    @Test
    void listCustomers_WhenRepositoryThrowsException_ShouldPropagate() {
        // Arrange
        when(customerRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(
            RuntimeException.class,
            () -> adminController.listCustomers()
        );
    }
}
