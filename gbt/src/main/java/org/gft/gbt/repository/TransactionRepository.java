package org.gft.gbt.repository;

import java.util.List;
import org.gft.gbt.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
