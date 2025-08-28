package org.gft.gbt.repository;

import org.gft.gbt.model.Fund;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FundRepository extends MongoRepository<Fund, Integer> {
}
