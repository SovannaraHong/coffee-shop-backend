package com.coffee_shop.coffee_shop.service;

import com.coffee_shop.coffee_shop.dto.request.SupplierCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.SupplierUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.PurchaseOrderResponse;
import com.coffee_shop.coffee_shop.dto.response.SupplierResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface SupplierService {

    SupplierResponse createSupplier(SupplierCreateRequest request);

    SupplierResponse updateSupplier(Long id, SupplierUpdateRequest request);

    void deleteSupplier(Long id);

    SupplierResponse getSupplierById(Long id);

    List<SupplierResponse> getAllSuppliers();

    Page<SupplierResponse> getAllSuppliers(Map<String, String> params);


    void activateSupplier(Long id);

    void deactivateSupplier(Long id);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<PurchaseOrderResponse> getPurchaseOrdersBySupplier(Long supplierId);
}
