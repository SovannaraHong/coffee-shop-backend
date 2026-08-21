package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermissionUpdateRequest {
    @NotBlank(message = "Permission name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;
}
