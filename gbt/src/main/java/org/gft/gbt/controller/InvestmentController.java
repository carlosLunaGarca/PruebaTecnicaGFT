package org.gft.gbt.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gft.gbt.model.Transaction;
import org.gft.gbt.service.InvestmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/{customerId}")
@Tag(name = "Inversiones", description = "Operaciones de suscripción, cancelación y consulta de transacciones")

public class InvestmentController {
    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @PostMapping("/funds/{fundId}/subscribe")
    @Operation(summary = "Suscribir a un fondo", description = "Crea una suscripción al fondo indicado para el cliente.")

    public ResponseEntity<Void> subscribe(@PathVariable String customerId, @PathVariable Integer fundId) {
        investmentService.subscribe(customerId, fundId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/funds/{fundId}/cancel")
    @Operation(summary = "Cancelar suscripción", description = "Cancela la suscripción del cliente al fondo indicado.")
    public ResponseEntity<Void> cancel(@PathVariable String customerId, @PathVariable Integer fundId) {
        investmentService.cancel(customerId, fundId);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Historial de transacciones", description = "Obtiene las transacciones del cliente (aperturas y cancelaciones).")
    @GetMapping("/transactions")
    public List<Transaction> transactions(@PathVariable String customerId) {
        return investmentService.getTransactions(customerId);
    }
}
