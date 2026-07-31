package com.coffee_shop.coffee_shop.specification.purchaseOrder;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PurchaseOrderFilter {
    private Long id;
    private Long supplierId;
    private String status; // matches PurchaseOrderStatus enum name as string
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}