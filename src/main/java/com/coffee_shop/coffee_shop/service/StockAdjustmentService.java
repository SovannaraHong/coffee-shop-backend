package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.response.StockAdjustmentResponse;
import com.coffee_shop.coffee_shop.specification.stockAdjustment.StockAdjustmentFilter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface StockAdjustmentService {

    List<StockAdjustmentResponse> getAdjustmentHistory(Long ingredientId);

    List<StockAdjustmentResponse> getAll();

    Page<StockAdjustmentResponse> getAllAdjustments(Map<String, String> params);

    List<StockAdjustmentResponse> searchAdjustments(StockAdjustmentFilter filter);

    StockAdjustmentResponse getAdjustmentById(Long id);
}