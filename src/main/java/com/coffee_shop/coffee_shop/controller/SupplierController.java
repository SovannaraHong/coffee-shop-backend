package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.SupplierCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.SupplierUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.PurchaseOrderResponse;
import com.coffee_shop.coffee_shop.dto.response.SupplierResponse;
import com.coffee_shop.coffee_shop.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("api/suppliers")
@RestController
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody SupplierUpdateRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SupplierResponse>> getAll() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping
    public ResponseEntity<PageDTO<SupplierResponse>> getAllPaged(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(new PageDTO<>(supplierService.getAllSuppliers(params)));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable Long id) {
        supplierService.activateSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        supplierService.deactivateSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> exists(@RequestParam(required = false) String email,
                                          @RequestParam(required = false) String phone) {
        if (email != null) {
            return ResponseEntity.ok(supplierService.existsByEmail(email));
        }
        if (phone != null) {
            return ResponseEntity.ok(supplierService.existsByPhone(phone));
        }
        return ResponseEntity.ok(false);
    }

    @GetMapping("/{id}/purchase-orders")
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrders(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getPurchaseOrdersBySupplier(id));
    }
}