package com.coffee_shop.coffee_shop.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CutLuyPaymentResponse(
        String id,
        String status,        // pending | scanned | paid | expired | failed
        String amount,
        String currency,
        String reference_id,
        String qr_string,
        String checkout_url,
        String approved_at,
        String created_at,
        String expires_at
) {
}