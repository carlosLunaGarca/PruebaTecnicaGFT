package org.gft.gbt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.gft.gbt.model.Fund;
import org.gft.gbt.repository.FundRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/funds")
@Tag(name = "Fondos", description = "Operaciones de consulta de fondos")
public class FundController {

    private final FundRepository fundRepository;

    public FundController(FundRepository fundRepository) {
        this.fundRepository = fundRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar fondos", description = "Obtiene la lista de todos los fondos disponibles")
    public List<Fund> listFunds() {
        return fundRepository.findAll();
    }
}
