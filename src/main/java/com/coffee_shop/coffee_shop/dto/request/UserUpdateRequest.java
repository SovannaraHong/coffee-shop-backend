package com.coffee_shop.coffee_shop.dto.request;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String fullName;
    private String email;

    private String password;

    private Long roleId;
}
