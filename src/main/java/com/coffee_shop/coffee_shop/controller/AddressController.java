package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.AddressCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.AddressUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddressResponse;
import com.coffee_shop.coffee_shop.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody AddressCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AddressUpdateRequest request) {
        return ResponseEntity.ok(addressService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AddressResponse>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(addressService.getByCustomerId(customerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}