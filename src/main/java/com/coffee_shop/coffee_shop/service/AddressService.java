package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.AddressCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.AddressUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {

    AddressResponse create(AddressCreateRequest request);

    AddressResponse update(Long id, AddressUpdateRequest request);

    AddressResponse getById(Long id);

    List<AddressResponse> getByCustomerId(Long customerId);

    void delete(Long id);
}