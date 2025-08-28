package org.gft.gbt.controller;

import java.math.BigDecimal;
import java.util.List;
import org.gft.gbt.model.Customer;
import org.gft.gbt.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/customers")
public class CustomerAdminController {
    private final CustomerRepository customerRepository;

    public CustomerAdminController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public List<Customer> list() {
        return customerRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer create(@RequestBody Customer customer) {
        if (customer.getBalance() == null) {
            customer.setBalance(new BigDecimal("500000"));
        }
        return customerRepository.save(customer);
    }
}
