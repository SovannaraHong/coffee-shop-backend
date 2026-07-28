package com.coffee_shop.coffee_shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationDTO {
    int pageNumber;
    int pageSize;
    long totalElements;
    int totalPage;
    boolean last;
    boolean first;
    boolean empty;
    int numberOfElements;
}