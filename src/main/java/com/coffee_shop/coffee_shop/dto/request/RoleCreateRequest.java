package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleCreateRequest {
    @NotBlank(message = "Role name is required")
    @Size(max = 100)
    private String name;

    private List<Long> permissionIds;
}