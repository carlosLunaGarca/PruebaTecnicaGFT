package org.gft.gbt.controller;

import java.util.List;
import org.gft.gbt.model.Fund;
import org.gft.gbt.repository.FundRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/funds")
public class FundController {
    private final FundRepository fundRepository;

    public FundController(FundRepository fundRepository) {
        this.fundRepository = fundRepository;
    }

    @GetMapping
    public List<Fund> list() {
        return fundRepository.findAll();
    }
}
