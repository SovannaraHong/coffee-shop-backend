package com.coffee_shop.coffee_shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRatingSummaryResponse {
    private Long productId;
    private Double averageRating; // null if no reviews yet
    private Long totalReviews;
}