package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.ReviewCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.ReviewUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.ProductRatingSummaryResponse;
import com.coffee_shop.coffee_shop.dto.response.ReviewResponse;
import com.coffee_shop.coffee_shop.entity.Customer;
import com.coffee_shop.coffee_shop.entity.Product;
import com.coffee_shop.coffee_shop.entity.Review;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.ReviewMapper;
import com.coffee_shop.coffee_shop.repository.CustomerRepository;
import com.coffee_shop.coffee_shop.repository.OrderDetailRepository;
import com.coffee_shop.coffee_shop.repository.ProductRepository;
import com.coffee_shop.coffee_shop.repository.ReviewRepository;
import com.coffee_shop.coffee_shop.service.ReviewService;
import com.coffee_shop.coffee_shop.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse create(Long customerId, ReviewCreateRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Customer", customerId));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Product", request.getProductId()));

        // enforce: must have actually bought and received this product
        boolean purchased = orderDetailRepository.existsCompletedPurchase(customerId, request.getProductId());
        if (!purchased) {
            throw new BadRequestException("You can only review products you have purchased and received.");
        }

        // enforce: one review per customer per product (DB unique constraint backs this up too)
        if (reviewRepository.existsByCustomerIdAndProductId(customerId, request.getProductId())) {
            throw new BadRequestException("You have already reviewed this product. Please edit your existing review instead.");
        }

        Review review = Review.builder()
                .customer(customer)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public ReviewResponse update(Long customerId, Long reviewId, ReviewUpdateRequest request) {
        Review review = findRequired(reviewId);

        if (!review.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("You can only edit your own reviews.");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void delete(Long customerId, Long reviewId) {
        Review review = findRequired(reviewId);

        if (!review.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("You can only delete your own reviews.");
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional
    public void deleteAsStaff(Long reviewId) {
        Review review = findRequired(reviewId);
        reviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse findById(Long id) {
        return reviewMapper.toResponse(findRequired(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<ReviewResponse> findByProduct(Long productId, Map<String, String> params) {
        Pageable pageable = PageUtil.getPageable(params);
        Page<ReviewResponse> page = reviewRepository.findByProductId(productId, pageable)
                .map(reviewMapper::toResponse);
        return new PageDTO<>(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<ReviewResponse> findByCustomer(Long customerId, Map<String, String> params) {
        Pageable pageable = PageUtil.getPageable(params);
        Page<ReviewResponse> page = reviewRepository.findByCustomerId(customerId, pageable)
                .map(reviewMapper::toResponse);
        return new PageDTO<>(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductRatingSummaryResponse getProductRatingSummary(Long productId) {
        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        Long count = reviewRepository.countByProductId(productId);

        return ProductRatingSummaryResponse.builder()
                .productId(productId)
                .averageRating(avg) // null if no reviews — let the frontend show "No reviews yet"
                .totalReviews(count != null ? count : 0L)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canReview(Long customerId, Long productId) {
        boolean purchased = orderDetailRepository.existsCompletedPurchase(customerId, productId);
        boolean alreadyReviewed = reviewRepository.existsByCustomerIdAndProductId(customerId, productId);
        return purchased && !alreadyReviewed;
    }

    private Review findRequired(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Review", id));
    }
}