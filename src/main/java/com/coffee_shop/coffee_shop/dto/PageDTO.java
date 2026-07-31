package com.coffee_shop.coffee_shop.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
public class PageDTO<T> {
    private List<T> list;
    private PaginationDTO paginationDTO;

    public PageDTO(Page<T> page) {
        this.list = page.getContent();
        this.paginationDTO = PaginationDTO.builder()
                .empty(page.isEmpty())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .pageNumber(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPage(page.getTotalPages())
                .build();
    }
}