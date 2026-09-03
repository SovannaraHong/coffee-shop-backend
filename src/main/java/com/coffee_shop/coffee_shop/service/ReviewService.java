package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.ReviewCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.ReviewUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductRatingSummaryResponse;
import com.coffee_shop.coffee_shop.dto.response.ReviewResponse;

import java.util.Map;

public interface ReviewService {

    // customer creates a review — only allowed if they actually bought and received the product
    ReviewResponse create(Long customerId, ReviewCreateRequest request);

    // customer edits their own review
    ReviewResponse update(Long customerId, Long reviewId, ReviewUpdateRequest request);

    // customer deletes their own review
    void delete(Long customerId, Long reviewId);

    // admin/staff can remove any review (e.g. abusive content)
    void deleteAsStaff(Long reviewId);

    ReviewResponse findById(Long id);

    // paginated reviews for a product's page (public-facing)
    PageDTO<ReviewResponse> findByProduct(Long productId, Map<String, String> params);

    // paginated reviews a specific customer has written (their own review history)
    PageDTO<ReviewResponse> findByCustomer(Long customerId, Map<String, String> params);

    // average rating + count, shown on product cards/listings
    ProductRatingSummaryResponse getProductRatingSummary(Long productId);

    // whether this customer is eligible to review this product (already purchased, not yet reviewed)
    boolean canReview(Long customerId, Long productId);
}