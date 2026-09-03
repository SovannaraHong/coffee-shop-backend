package com.coffee_shop.coffee_shop.specification.user;

import lombok.Data;

@Data
public class UserFilter {
    private String fullName;
    private String email;
    private Boolean isActive;
    private Long roleId;
}