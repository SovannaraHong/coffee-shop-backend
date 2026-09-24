package com.coffee_shop.coffee_shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private Long id;
    private Long customerId;
    private String label;
    private String addressLine;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Boolean isDefault;
}