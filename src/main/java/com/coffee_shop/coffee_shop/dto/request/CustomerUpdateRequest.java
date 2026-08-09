package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerUpdateRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 150)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 150)
    private String lastName;

    @Size(max = 20)
    private String phone;
}