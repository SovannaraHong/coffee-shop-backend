package com.coffee_shop.coffee_shop.controller;

import com.coffee_shop.coffee_shop.dto.request.AddonCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddonResponse;
import com.coffee_shop.coffee_shop.service.AddonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("api/addons")
@RestController
@RequiredArgsConstructor
public class AddonController {

    private final AddonService addonService;


    @Operation(
            summary = "Create addon",
            description = """
                    Create a new addon that customers can select when ordering products.
                    
                    Example:
                    Create an addon called Extra Cheese,
                    Extra Shot, Whipped Cream, or Syrup.
                    
                    Addon ingredients can be configured separately
                    using addon ingredient API.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Addon created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid addon data")
    })
    @PostMapping
    public ResponseEntity<AddonResponse> create(
            @Valid @RequestBody AddonCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(addonService.create(request));
    }


    @Operation(
            summary = "Update addon",
            description = """
                    Update addon information.
                    
                    Example:
                    Change addon name, price, or other addon details.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Addon updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid addon data"),
            @ApiResponse(responseCode = "404", description = "Addon not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AddonResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AddonCreateRequest request) {

        return ResponseEntity.ok(addonService.update(id, request));
    }


    @Operation(
            summary = "Delete addon",
            description = """
                    Delete an addon from the system.
                    
                    Example:
                    Remove an unavailable addon from customer selection.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Addon deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Addon not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        addonService.delete(id);

        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Get addon by id",
            description = """
                    Retrieve addon details by addon ID.
                    
                    Returns addon information including
                    name, price, and status.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Addon retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Addon not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AddonResponse> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(addonService.findById(id));
    }


    @Operation(
            summary = "Get all addons",
            description = """
                    Retrieve all available addons.
                    
                    Used for displaying addon options
                    when customers customize their orders.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Addons retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<AddonResponse>> getAll() {

        return ResponseEntity.ok(addonService.getAll());
    }


    @Operation(
            summary = "Change addon status",
            description = """
                    Toggle addon active status.
                    
                    Example:
                    Enable or disable an addon without deleting it.
                    
                    Disabled addons will not be available
                    for customer ordering.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Addon status changed successfully"),
            @ApiResponse(responseCode = "404", description = "Addon not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<AddonResponse> changeStatus(
            @PathVariable Long id) {

        return ResponseEntity.ok(addonService.changeStatus(id));
    }
}