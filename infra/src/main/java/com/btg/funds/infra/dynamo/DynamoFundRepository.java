package com.btg.funds.infra.dynamo;

import com.btg.funds.domain.model.Fund;
import com.btg.funds.domain.spi.FundRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * DynamoDB implementation of FundRepository.
 */
@Repository
public class DynamoFundRepository implements FundRepository {
    @Override
    public List<Fund> findAll() {
        return Collections.emptyList();
    }
}
