package org.gft.gbt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.gft.gbt.handler.GlobalExceptionHandler;
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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        // Configuración mejorada de MockMvc con UTF-8
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setDefaultCharset(StandardCharsets.UTF_8);
        
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .defaultRequest(get("/").characterEncoding("UTF-8"))
                .alwaysExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .build();
        
        testCustomer = new Customer(
            "test-customer-1",
            new BigDecimal("1000000"),
            NotificationPreference.EMAIL
        );
    }

    // ... existing code ...

    @Test
    void listCustomers_ShouldReturnAllCustomers() throws Exception {
        // Arrange
        List<Customer> expectedCustomers = Arrays.asList(
            testCustomer,
            new Customer("test-customer-2", new BigDecimal("500000"), 
                        NotificationPreference.SMS)
        );
        when(customerRepository.findAll()).thenReturn(expectedCustomers);

        // Act & Assert
        mockMvc.perform(get("/api/admin/customers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("test-customer-1")))
                .andExpect(jsonPath("$[1].id", is("test-customer-2")));

        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void createCustomer_WithNewCustomer_ShouldReturnCreatedCustomer() throws Exception {
        // Arrange
        Customer newCustomer = new Customer("new-customer", new BigDecimal("1000"), 
                                         NotificationPreference.EMAIL);
        when(customerRepository.existsById(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(newCustomer);

        // Act & Assert
        mockMvc.perform(post("/api/admin/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("new-customer")))
                .andExpect(jsonPath("$.balance", is(1000)));

        verify(customerRepository, times(1)).existsById("new-customer");
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void createCustomer_WithExistingId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/admin/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCustomer)))
                .andExpect(status().isBadRequest());
        
        verify(customerRepository, times(1)).existsById(anyString());
        verify(customerRepository, never()).save(any());
    }

    // ... existing code ...
    void createCustomer_WithDuplicateKey_ShouldThrowException() {
        // Arrange
        when(customerRepository.existsById(anyString())).thenReturn(false);
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
    void createCustomer_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Test con ID vacío
        Customer invalidCustomerEmptyId = new Customer("", new BigDecimal("1000"), NotificationPreference.EMAIL);
        
        mockMvc.perform(post("/api/admin/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCustomerEmptyId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("El ID del cliente es requerido")));
        
        // Test con balance negativo  
        Customer invalidCustomerNegativeBalance = new Customer("test-id-negative", new BigDecimal("-1000"), NotificationPreference.EMAIL);
        
        mockMvc.perform(post("/api/admin/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCustomerNegativeBalance)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("El balance del cliente debe ser mayor o igual a cero")));
        
        // Test con preferencia de notificación nula - crear un objeto con reflection o método personalizado
        String invalidJsonWithNullPreference = "{\"id\":\"test-id-null\",\"balance\":1000,\"notificationPreference\":null}";
        
        mockMvc.perform(post("/api/admin/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJsonWithNullPreference))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("La preferencia de notificación es requerida")));
        
        verify(customerRepository, never()).save(any());
        verify(customerRepository, never()).existsById(anyString());
    }

    @Test
    void listCustomers_WhenRepositoryThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(customerRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(get("/api/admin/customers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
