package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.OrderCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.OrderResponse;

import java.util.List;
import java.util.Map;

public interface OrderService {

    OrderResponse create(OrderCreateRequest request);

    OrderResponse findById(Long id);

    List<OrderResponse> findByCustomerId(Long customerId);

    List<OrderResponse> getAll();

    PageDTO<OrderResponse> getPagination(Map<String, String> params);

    OrderResponse confirm(Long id);

    OrderResponse cancel(Long id);

    OrderResponse markPreparing(Long id);

    OrderResponse markReady(Long id);

    OrderResponse markDelivering(Long id);

    OrderResponse complete(Long id);
}