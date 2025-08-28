package org.gft.gbt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gft.gbt.model.Customer;
import org.gft.gbt.repository.CustomerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Operaciones de administración")
public class AdminController {

    private final CustomerRepository customerRepository;

    public AdminController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar clientes", description = "Obtiene la lista de todos los clientes")
    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    @PostMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear cliente", description = "Crea un nuevo cliente")
    public Customer createCustomer(@RequestBody Customer customer) {
        if (customer.getId() != null && customerRepository.existsById(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un cliente con este ID");
        }
        return customerRepository.save(customer);
    }
}
