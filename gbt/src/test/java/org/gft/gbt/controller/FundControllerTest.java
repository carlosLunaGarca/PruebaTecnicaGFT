package org.gft.gbt.controller;

import org.gft.gbt.model.Fund;
import org.gft.gbt.repository.FundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FundControllerTest {

    @Mock
    private FundRepository fundRepository;

    @InjectMocks
    private FundController fundController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser
    void listFunds_ShouldReturnAllFunds() {
        // Arrange
        List<Fund> expectedFunds = Arrays.asList(
                new Fund(1, "Test Fund 1", new BigDecimal("1000"), "FIC"),
                new Fund(2, "Test Fund 2", new BigDecimal("2000"), "FPV")
        );
        when(fundRepository.findAll()).thenReturn(expectedFunds);

        // Act
        List<Fund> result = fundController.listFunds();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Fund 1", result.get(0).getName());
        verify(fundRepository, times(1)).findAll();
    }

    @Test
    @WithMockUser
    void listFunds_WhenNoFunds_ShouldReturnEmptyList() {
        // Arrange
        when(fundRepository.findAll()).thenReturn(List.of());

        // Act
        List<Fund> result = fundController.listFunds();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(fundRepository, times(1)).findAll();
    }
}
