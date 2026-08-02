package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.SupplierCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.SupplierUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.PurchaseOrderResponse;
import com.coffee_shop.coffee_shop.dto.response.SupplierResponse;
import com.coffee_shop.coffee_shop.entity.Supplier;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.PurchaseOrderMapper;
import com.coffee_shop.coffee_shop.mapper.SupplierMapper;
import com.coffee_shop.coffee_shop.repository.SupplierRepository;
import com.coffee_shop.coffee_shop.service.SupplierService;
import com.coffee_shop.coffee_shop.specification.supplier.SupplierFilter;
import com.coffee_shop.coffee_shop.specification.supplier.SupplierSpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;

    @Transactional
    @Override
    public SupplierResponse createSupplier(SupplierCreateRequest request) {
        if (supplierRepository.findByNameIgnoreCase(request.getName()).isPresent()) {
            throw BadRequestException.alreadyExits("Supplier", request.getName());
        }

        if (request.getEmail() != null && existsByEmail(request.getEmail())) {
            throw BadRequestException.alreadyExits("Supplier", request.getEmail());
        }

        if (request.getPhone() != null && existsByPhone(request.getPhone())) {
            throw BadRequestException.alreadyExits("Supplier", request.getPhone());
        }
        Supplier entity = supplierMapper.toEntity(request);
        return supplierMapper.toSupplierResponse(supplierRepository.save(entity));
    }

    @Transactional
    @Override
    public SupplierResponse updateSupplier(Long id, SupplierUpdateRequest request) {
        Supplier supplierEntityById = findSupplierEntityById(id);

        supplierRepository.findByNameIgnoreCase(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw BadRequestException.alreadyExits("Supplier", request.getName());
                });

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            supplierRepository.findByEmail(request.getEmail())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw BadRequestException.alreadyExits("Supplier", request.getEmail());
                    });
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            supplierRepository.findByPhone(request.getPhone())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw BadRequestException.alreadyExits("Supplier", request.getPhone());
                    });
        }

        supplierMapper.toUpdate(supplierEntityById, request);
        return supplierMapper.toSupplierResponse(supplierRepository.save(supplierEntityById));
    }

    @Transactional
    @Override
    public void deleteSupplier(Long id) {
        Supplier supplierById = findSupplierEntityById(id);
        supplierRepository.delete(supplierById);

    }

    @Transactional(readOnly = true)
    @Override
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.notFoundException("Supplier", id));
        return supplierMapper.toSupplierResponse(supplier);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SupplierResponse> getAllSuppliers() {
        List<Supplier> suppliers = supplierRepository.findAll();

        return suppliers.stream()
                .map(supplierMapper::toSupplierResponse)
                .toList();
    }

    @Transactional
    @Override
    public Page<SupplierResponse> getAllSuppliers(Map<String, String> params) {
        SupplierFilter filter = new SupplierFilter();

        if (params.containsKey("keyword")) {
            filter.setKeyword(params.get("keyword"));
        }
        if (params.containsKey("isActive")) {
            filter.setIsActive(Boolean.valueOf(params.get("isActive")));
        }

        SupplierSpec spec = new SupplierSpec(filter);
        Pageable pageable = PageUtil.getPageable(params);

        return supplierRepository.findAll(spec, pageable)
                .map(supplierMapper::toSupplierResponse);
    }

    @Transactional
    @Override
    public void activateSupplier(Long id) {
        Supplier supplierEntityById = findSupplierEntityById(id);
        supplierEntityById.setIsActive(true);
        supplierRepository.save(supplierEntityById);

    }

    @Transactional
    @Override
    public void deactivateSupplier(Long id) {
        Supplier supplierEntityById = findSupplierEntityById(id);
        supplierEntityById.setIsActive(false);
        supplierRepository.save(supplierEntityById);

    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return supplierRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return supplierRepository.existsByPhone(phone);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PurchaseOrderResponse> getPurchaseOrdersBySupplier(Long supplierId) {
        Supplier supplierEntityById = findSupplierEntityById(supplierId);
        return supplierEntityById.getPurchaseOrders().stream().map(purchaseOrderMapper::toResponse).toList();

    }

    @Transactional(readOnly = true)
    private Supplier findSupplierEntityById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Supplier", id));
    }
}
