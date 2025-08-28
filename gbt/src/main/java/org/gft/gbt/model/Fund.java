package org.gft.gbt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "funds")
@NoArgsConstructor
@AllArgsConstructor
public class Fund {
    @Id
    private Integer id;
    private String name;
    private BigDecimal minimumAmount;
    private String category;
}

