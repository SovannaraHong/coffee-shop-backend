package com.coffee_shop.coffee_shop.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotEmpty(message = "Product name is require.")
    private String name;

    private String description;

    @NotNull(message = "Price is can not be null.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0.0")
    @Digits(integer = 10, fraction = 2, message = "Price format invalid.")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100")
    private BigDecimal discount;

    @Min(value = 0, message = "Stock can not be negative")
    private Integer stock;
    private String image;
    private String size;
    private Boolean status;
    @NotNull(message = "Category is require")
    private Long categoryId;
}
