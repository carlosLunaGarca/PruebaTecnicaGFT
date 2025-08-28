package com.btg.funds.domain.spi;

import com.btg.funds.domain.model.Fund;
import java.util.List;

/** Port for accessing funds catalog. */
public interface FundRepository {
    List<Fund> findAll();
}
