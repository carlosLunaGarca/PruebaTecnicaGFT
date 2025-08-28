package org.gft.gbt.controller;

import java.util.List;
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
public class InvestmentController {
    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @PostMapping("/funds/{fundId}/subscribe")
    public ResponseEntity<Void> subscribe(@PathVariable String customerId, @PathVariable Integer fundId) {
        investmentService.subscribe(customerId, fundId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/funds/{fundId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String customerId, @PathVariable Integer fundId) {
        investmentService.cancel(customerId, fundId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/transactions")
    public List<Transaction> transactions(@PathVariable String customerId) {
        return investmentService.getTransactions(customerId);
    }
}
