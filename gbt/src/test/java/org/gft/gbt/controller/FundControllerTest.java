package org.gft.gbt.controller;

import org.gft.gbt.model.Fund;
import org.gft.gbt.repository.FundRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundControllerTest {

    @Mock
    private FundRepository fundRepository;

    @InjectMocks
    private FundController fundController;

    @Test
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
        assertEquals("Test Fund 2", result.get(1).getName());
        verify(fundRepository, times(1)).findAll();
    }

    @Test
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

    @Test
    void listFunds_WhenRepositoryThrowsException_ShouldPropagate() {
        // Arrange
        when(fundRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> fundController.listFunds());
        verify(fundRepository, times(1)).findAll();
    }
}
