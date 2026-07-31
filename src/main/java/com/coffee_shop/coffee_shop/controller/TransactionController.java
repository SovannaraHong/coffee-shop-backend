package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.response.TransactionResponse;
import com.coffee_shop.coffee_shop.service.InventoryTransactionService;
import com.coffee_shop.coffee_shop.util.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final InventoryTransactionService inventoryTransactionService;

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll() {
        return ResponseEntity.ok(inventoryTransactionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryTransactionService.getById(id));
    }

    @GetMapping("/pagination")
    public ResponseEntity<PageDTO<TransactionResponse>> getTransactions(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(inventoryTransactionService.getPagination(params));
    }

    @GetMapping("/ingredient/{ingredientId}")
    public ResponseEntity<List<TransactionResponse>> getByIngredient(@PathVariable Long ingredientId) {
        return ResponseEntity.ok(inventoryTransactionService.getByIngredient(ingredientId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<TransactionResponse>> getByType(@PathVariable TransactionType type) {
        return ResponseEntity.ok(inventoryTransactionService.getByType(type));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(inventoryTransactionService.getByDateRange(start, end));
    }
}