package com.coffee_shop.coffee_shop.dto.response;

import com.coffee_shop.coffee_shop.util.enums.PurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private LocalDateTime orderDate;
    private PurchaseOrderStatus status;
    private BigDecimal totalAmount;
    private List<PurchaseOrderDetailResponse> details;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PurchaseOrderDetailResponse {
        private Long id;
        private Long ingredientId;
        private String ingredientName;
        private BigDecimal quantity;
        private BigDecimal unitCost;
        private BigDecimal subtotal;
    }
}