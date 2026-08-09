package com.coffee_shop.coffee_shop.dto.request;
//package com.coffee_shop.coffee_shop.dto.request;
//
//import java.math.BigDecimal;
//
//public record CreateCutLuyPaymentRequest(
//        BigDecimal amount,
//        String reference_id
//) {
//}

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCutLuyPaymentRequest {

    private BigDecimal amount;
    private String reference_id;
}