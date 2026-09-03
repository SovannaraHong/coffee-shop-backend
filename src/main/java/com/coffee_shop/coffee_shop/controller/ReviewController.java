package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.ReviewCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.ReviewUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductRatingSummaryResponse;
import com.coffee_shop.coffee_shop.dto.response.ReviewResponse;
import com.coffee_shop.coffee_shop.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // --- customer-facing (requires CUSTOMER role) ---

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("api/reviews")
    public ResponseEntity<ReviewResponse> create(Authentication authentication,
                                                 @Valid @RequestBody ReviewCreateRequest request) {
        Long customerId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(customerId, request));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("api/reviews/{id}")
    public ResponseEntity<ReviewResponse> update(Authentication authentication,
                                                 @PathVariable Long id,
                                                 @Valid @RequestBody ReviewUpdateRequest request) {
        Long customerId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(reviewService.update(customerId, id, request));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @DeleteMapping("api/reviews/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        Long customerId = (Long) authentication.getPrincipal();
        reviewService.delete(customerId, id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("api/reviews/me")
    public ResponseEntity<PageDTO<ReviewResponse>> myReviews(Authentication authentication,
                                                             @RequestParam Map<String, String> params) {
        Long customerId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(reviewService.findByCustomer(customerId, params));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("api/products/{productId}/reviews/can-review")
    public ResponseEntity<Map<String, Boolean>> canReview(Authentication authentication,
                                                          @PathVariable Long productId) {
        Long customerId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of("canReview", reviewService.canReview(customerId, productId)));
    }


    @GetMapping("api/products/{productId}/reviews")
    public ResponseEntity<PageDTO<ReviewResponse>> findByProduct(@PathVariable Long productId,
                                                                 @RequestParam Map<String, String> params) {
        return ResponseEntity.ok(reviewService.findByProduct(productId, params));
    }

    @GetMapping("api/products/{productId}/reviews/summary")
    public ResponseEntity<ProductRatingSummaryResponse> ratingSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductRatingSummary(productId));
    }

    // --- staff moderation ---

    @PreAuthorize("hasAuthority('REVIEW_MODERATE')")
    @DeleteMapping("api/staff/reviews/{id}")
    public ResponseEntity<Void> deleteAsStaff(@PathVariable Long id) {
        reviewService.deleteAsStaff(id);
        return ResponseEntity.noContent().build();
    }
}