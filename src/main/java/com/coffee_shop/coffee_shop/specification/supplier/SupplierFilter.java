package com.coffee_shop.coffee_shop.specification.supplier;

import lombok.Data;

@Data
public class SupplierFilter {
    private String keyword;      // matches name / contactPerson / email
    private Boolean isActive;
}