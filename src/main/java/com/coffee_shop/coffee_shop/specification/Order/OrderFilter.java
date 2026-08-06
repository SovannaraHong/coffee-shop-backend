package com.coffee_shop.coffee_shop.specification.Order;

import com.coffee_shop.coffee_shop.util.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderFilter {
    private Long customerId;
    private OrderStatus status;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}