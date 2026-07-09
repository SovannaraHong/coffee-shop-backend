package com.coffee_shop.coffee_shop.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

public interface PageUtil {
    int DEFAULT_PAGE_NUMBER = 1;
    int DEFAULT_PAGE_SIZE = 10;
    String DEFAULT_NUMBER = "_page";
    String DEFAULT_LIMIT = "_limit";

    static Pageable PAGEABLE(int number, int size) {
        if (number < DEFAULT_PAGE_NUMBER) {
            number = DEFAULT_PAGE_NUMBER;
        }
        if (size < DEFAULT_PAGE_SIZE) {
            size = DEFAULT_PAGE_SIZE;
        }
        return PageRequest.of(number - 1, size);
    }

    static Pageable getPageable(Map<String, String> params) {
        int page = DEFAULT_PAGE_NUMBER;
        int size = DEFAULT_PAGE_SIZE;
        String sortBy = params.getOrDefault("sortBy", "id");
        String direction = params.getOrDefault("direction", "asc");
        if (params.containsKey(DEFAULT_NUMBER)) {
            page = Integer.parseInt(params.get(DEFAULT_NUMBER));
        }
        if (params.containsKey(DEFAULT_LIMIT)) {
            size = Integer.parseInt(params.get(DEFAULT_LIMIT));
        }
        return PageRequest.of(page - 1, size, direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

    }
}
