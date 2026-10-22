package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.response.StockAdjustmentResponse;
import com.coffee_shop.coffee_shop.service.StockAdjustmentService;
import com.coffee_shop.coffee_shop.specification.stockAdjustment.StockAdjustmentFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/stock-adjustment")
@RequiredArgsConstructor
public class StockAdjustmentController {

    private final StockAdjustmentService stockAdjustmentService;

    // ── READ — all ────────────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('STOCK_ADJUSTMENT_READ', 'STOCK_ADJUSTMENT_MANAGE')")
    @GetMapping
    public ResponseEntity<List<StockAdjustmentResponse>> getAll() {
        return ResponseEntity.ok(stockAdjustmentService.getAll());
    }

    // ── READ — pagination ─────────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('STOCK_ADJUSTMENT_READ', 'STOCK_ADJUSTMENT_MANAGE')")
    @GetMapping("/paged")
    public ResponseEntity<PageDTO<StockAdjustmentResponse>> getAllAdjustments(
            @RequestParam Map<String, String> params) {
        Page<StockAdjustmentResponse> allAdjustments = stockAdjustmentService.getAllAdjustments(params);
        return ResponseEntity.ok().body(new PageDTO<>(allAdjustments));
    }

    // ── READ — history by ingredient ──────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('STOCK_ADJUSTMENT_READ', 'STOCK_ADJUSTMENT_MANAGE')")
    @GetMapping("/ingredient/{ingredientId}")
    public ResponseEntity<List<StockAdjustmentResponse>> getAdjustmentHistory(
            @PathVariable Long ingredientId) {
        return ResponseEntity.ok(stockAdjustmentService.getAdjustmentHistory(ingredientId));
    }

    // ── READ — search with filters ────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('STOCK_ADJUSTMENT_READ', 'STOCK_ADJUSTMENT_MANAGE')")
    @GetMapping("/search")
    public ResponseEntity<List<StockAdjustmentResponse>> searchAdjustments(StockAdjustmentFilter filter) {
        return ResponseEntity.ok(stockAdjustmentService.searchAdjustments(filter));
    }

    // ── READ — single by ID ───────────────────────────────────────────────────
    @PreAuthorize("hasAnyAuthority('STOCK_ADJUSTMENT_READ', 'STOCK_ADJUSTMENT_MANAGE')")
    @GetMapping("/{id}")
    public ResponseEntity<StockAdjustmentResponse> getAdjustmentById(@PathVariable Long id) {
        return ResponseEntity.ok(stockAdjustmentService.getAdjustmentById(id));
    }
}