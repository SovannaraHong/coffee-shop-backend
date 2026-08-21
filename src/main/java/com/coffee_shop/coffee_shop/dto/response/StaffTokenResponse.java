package com.coffee_shop.coffee_shop.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffTokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType; // "Bearer"
    private UserResponse user;
}