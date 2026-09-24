package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateRequest {

    @NotNull(message = "Customer id is required")
    private Long customerId;

    private String label;

    @NotBlank(message = "Address line is required")
    private String addressLine;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    @Builder.Default
    private Boolean isDefault = false;
}