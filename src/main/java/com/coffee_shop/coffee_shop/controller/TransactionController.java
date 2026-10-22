package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.response.TransactionResponse;
import com.coffee_shop.coffee_shop.service.InventoryTransactionService;
import com.coffee_shop.coffee_shop.util.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final InventoryTransactionService inventoryTransactionService;

    // ── READ — all ────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('TRANSACTION_READ', 'TRANSACTION_MANAGE')")
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll() {
        return ResponseEntity.ok(inventoryTransactionService.getAll());
    }

    // ── READ — single ─────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('TRANSACTION_READ', 'TRANSACTION_MANAGE')")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryTransactionService.getById(id));
    }

    // ── READ — pagination ─────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('TRANSACTION_READ', 'TRANSACTION_MANAGE')")
    @GetMapping("/pagination")
    public ResponseEntity<PageDTO<TransactionResponse>> getTransactions(
            @RequestParam Map<String, String> params) {
        return ResponseEntity.ok(inventoryTransactionService.getPagination(params));
    }

    // ── READ — filter by ingredient ───────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('TRANSACTION_READ', 'TRANSACTION_MANAGE')")
    @GetMapping("/ingredient/{ingredientId}")
    public ResponseEntity<List<TransactionResponse>> getByIngredient(@PathVariable Long ingredientId) {
        return ResponseEntity.ok(inventoryTransactionService.getByIngredient(ingredientId));
    }

    // ── READ — filter by transaction type ─────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('TRANSACTION_READ', 'TRANSACTION_MANAGE')")
    @GetMapping("/type/{type}")
    public ResponseEntity<List<TransactionResponse>> getByType(@PathVariable TransactionType type) {
        return ResponseEntity.ok(inventoryTransactionService.getByType(type));
    }

    // ── READ — filter by date range ───────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('TRANSACTION_READ', 'TRANSACTION_MANAGE')")
    @GetMapping("/date-range")
    public ResponseEntity<List<TransactionResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(inventoryTransactionService.getByDateRange(start, end));
    }
}