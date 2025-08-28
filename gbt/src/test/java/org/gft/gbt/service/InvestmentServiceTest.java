package org.gft.gbt.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.gft.gbt.model.Customer;
import org.gft.gbt.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InvestmentServiceTest {

    @Autowired
    private InvestmentService investmentService;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void clean() {
        Customer c = customerRepository.findById("1").orElseThrow();
        c.setBalance(new BigDecimal("500000"));
        c.getSubscriptions().clear();
        customerRepository.save(c);
    }

    @Test
    void subscribeShouldDeductBalance() {
        investmentService.subscribe("1", 1);
        Customer c = customerRepository.findById("1").orElseThrow();
        assertEquals(new BigDecimal("425000"), c.getBalance());
        assertTrue(c.getSubscriptions().contains(1));
    }

    @Test
    void cancelShouldRefundBalance() {
        investmentService.subscribe("1", 1);
        investmentService.cancel("1", 1);
        Customer c = customerRepository.findById("1").orElseThrow();
        assertEquals(new BigDecimal("500000"), c.getBalance());
        assertFalse(c.getSubscriptions().contains(1));
    }
}
