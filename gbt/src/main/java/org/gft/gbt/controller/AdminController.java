package org.gft.gbt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gft.gbt.model.Customer;
import org.gft.gbt.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Operaciones de administración")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final CustomerRepository customerRepository;

    public AdminController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar clientes", description = "Obtiene la lista de todos los clientes")
    public List<Customer> listCustomers() {
        try {
            logger.debug("Obteniendo lista de todos los clientes");
            List<Customer> customers = customerRepository.findAll();
            logger.debug("Se obtuvieron {} clientes exitosamente", customers.size());
            return customers;
        } catch (DataAccessException e) {
            logger.error("Error de acceso a datos al obtener lista de clientes: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, 
                "Servicio de base de datos temporalmente no disponible. Intente más tarde."
            );
        } catch (RuntimeException e) {
            logger.error("Error de runtime al obtener lista de clientes: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor al procesar la solicitud"
            );
        } catch (Exception e) {
            logger.error("Error inesperado al obtener lista de clientes: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor al procesar la solicitud"
            );
        }
    }

    @PostMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear cliente", description = "Crea un nuevo cliente")
    public Customer createCustomer(@RequestBody Customer customer) {
        try {
            logger.debug("Creando nuevo cliente con ID: {}", customer.getId());
            
            // Validaciones de datos de entrada
            if (customer.getId() == null || customer.getId().trim().isEmpty()) {
                logger.warn("Intento de crear cliente con ID vacío o nulo");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ID del cliente es requerido y no puede estar vacío");
            }
            
            if (customer.getBalance() == null || customer.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                logger.warn("Intento de crear cliente con balance inválido: {}", customer.getBalance());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El balance del cliente debe ser mayor o igual a cero");
            }
            
            if (customer.getNotificationPreference() == null) {
                logger.warn("Intento de crear cliente sin preferencia de notificación");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La preferencia de notificación es requerida");
            }
            
            // Verificación de ID duplicado (una sola vez)
            if (customerRepository.existsById(customer.getId())) {
                logger.warn("Intento de crear cliente con ID existente: {}", customer.getId());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un cliente con este ID");
            }
            
            Customer savedCustomer = customerRepository.save(customer);
            logger.info("Cliente creado exitosamente con ID: {}", savedCustomer.getId());
            return savedCustomer;
            
        } catch (ResponseStatusException e) {
            // Re-lanzar ResponseStatusException tal como está
            throw e;
        } catch (org.springframework.dao.DuplicateKeyException e) {
            logger.warn("Intento de crear cliente con clave duplicada: {}", e.getMessage());
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ya existe un cliente con este identificador único"
            );
        } catch (DataAccessException e) {
            logger.error("Error de acceso a datos al crear cliente: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Servicio de base de datos temporalmente no disponible. Intente más tarde."
            );
        } catch (Exception e) {
            logger.error("Error inesperado al crear cliente: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor al crear el cliente"
            );
        }
    }
}
