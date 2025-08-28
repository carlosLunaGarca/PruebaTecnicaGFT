package org.gft.gbt.config;

import java.math.BigDecimal;
import java.util.List;
import org.gft.gbt.model.Customer;
import org.gft.gbt.model.Fund;
import org.gft.gbt.model.NotificationPreference;
import org.gft.gbt.repository.CustomerRepository;
import org.gft.gbt.repository.FundRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(FundRepository fundRepository, CustomerRepository customerRepository) {
        return args -> {
            if (fundRepository.count() == 0) {
                fundRepository.saveAll(List.of(
                        new Fund(1, "FPV_BTG_PACTUAL_RECAUDADORA", new BigDecimal("75000"), "FPV"),
                        new Fund(2, "FPV_BTG_PACTUAL_ECOPETROL", new BigDecimal("125000"), "FPV"),
                        new Fund(3, "DEUDAPRIVADA", new BigDecimal("50000"), "FIC"),
                        new Fund(4, "FDO-ACCIONES", new BigDecimal("250000"), "FIC"),
                        new Fund(5, "FPV_BTG_PACTUAL_DINAMICA", new BigDecimal("100000"), "FPV")
                ));
            }
            if (customerRepository.count() == 0) {
                customerRepository.save(new Customer("1", new BigDecimal("500000"), NotificationPreference.EMAIL));
            }
        };
    }
}
