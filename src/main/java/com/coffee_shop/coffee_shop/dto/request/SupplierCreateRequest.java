package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierCreateRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 150)
    private String contactPerson;

    @Pattern(regexp = "^$|^[0-9+\\-() ]{7,20}$", message = "Invalid phone number")
    private String phone;

    @Email(message = "Invalid email")
    @Size(max = 150)
    private String email;

    @Size(max = 255)
    private String address;

}

