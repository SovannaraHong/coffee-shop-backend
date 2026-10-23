package com.coffee_shop.coffee_shop.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class AssignAddonsRequest {
    private Set<Long> addonIds;
}
