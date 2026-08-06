package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    @NotNull(message = "Customer id is required")
    private Long customerId;

    private Long addressId; // optional — null if pickup, not delivery

    @Size(max = 255)
    private String note;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderDetailRequest> details;
}