package com.coffee_shop.coffee_shop.dto.response;

import com.coffee_shop.coffee_shop.util.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({
        "id",
        "orderNumber",
        "customerId",
        "customerName",
        "status",
        "totalAmount",
        "discountAmount",
        "taxAmount",
        "finalAmount",
        "note",
        "orderDate",
        "details"
})
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal finalAmount;
    private String note;
    private LocalDateTime orderDate;
    private List<OrderDetailResponse> details;
}