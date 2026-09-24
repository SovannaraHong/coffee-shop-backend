package com.coffee_shop.coffee_shop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressUpdateRequest {

    private String label;

    private String addressLine;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    private Boolean isDefault;
}