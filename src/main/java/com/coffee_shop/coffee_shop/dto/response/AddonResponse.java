package com.coffee_shop.coffee_shop.dto.response;

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
public class AddonResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Boolean isActive;
    private LocalDateTime createdAt;
}