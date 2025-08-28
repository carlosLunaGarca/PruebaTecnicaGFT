package com.btg.funds.app.controller;

import com.btg.funds.domain.model.Fund;
import com.btg.funds.domain.spi.FundRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** REST controller exposing funds. */
@RestController
@RequestMapping("/funds")
public class FundController {

    private final FundRepository repository;

    public FundController(FundRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Fund> listFunds() {
        return repository.findAll();
    }
}
