package com.coffee_shop.coffee_shop.specification.transaction;


import lombok.Data;

import java.time.LocalDateTime;

@Data

public class TransactionFilter {
    private Long id;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
