package com.coffee_shop.coffee_shop.dto.response;

import com.coffee_shop.coffee_shop.util.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private String referenceType;
    private TransactionType type;
    private Long referenceId;
    private LocalDateTime transactionDate;
    private String note;
}