package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.response.TransactionResponse;
import com.coffee_shop.coffee_shop.util.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface InventoryTransactionService {
    List<TransactionResponse> getAll();

    TransactionResponse getById(Long id);

    PageDTO<TransactionResponse> getPagination(Map<String, String> params);

    List<TransactionResponse> getByIngredient(Long ingredientId);

    List<TransactionResponse> getByType(TransactionType type);

    List<TransactionResponse> getByDateRange(LocalDateTime start, LocalDateTime end);

    TransactionResponse getSummary();

}
