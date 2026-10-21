package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.VariantRequest;
import com.coffee_shop.coffee_shop.dto.response.VariantResponse;
import com.coffee_shop.coffee_shop.entity.Product;
import com.coffee_shop.coffee_shop.entity.Variant;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.VariantMapper;
import com.coffee_shop.coffee_shop.repository.ProductRepository;
import com.coffee_shop.coffee_shop.repository.VariantRepository;
import com.coffee_shop.coffee_shop.service.ProductService;
import com.coffee_shop.coffee_shop.service.VariantService;
import com.coffee_shop.coffee_shop.specification.variant.VariantFilter;
import com.coffee_shop.coffee_shop.specification.variant.VariantSpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {

    private final VariantRepository variantRepository;
    private final ProductRepository productRepository;
    private final VariantMapper variantMapper;
    private final ProductService productService;

    @Override
    @Transactional
    public VariantResponse create(Long productId, VariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Product", productId));

        request.setSku(normalizeSku(request.getSku()));

        variantRepository.findByProductIdAndNameIgnoreCase(productId, request.getName())
                .ifPresent(v -> {
                    throw BadRequestException.alreadyExits("Variant", v.getId(), request.getName());
                });
        checkSkuFree(request.getSku(), null);

        Variant entity = variantMapper.toEntity(request);
        entity.setProduct(product);
        return variantMapper.toResponse(variantRepository.save(entity));
    }

    @Override
    @Transactional
    public VariantResponse update(Long id, VariantRequest request) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Variant", id));

        request.setSku(normalizeSku(request.getSku()));
        Long productId = variant.getProduct().getId();

        if (!variant.getName().equalsIgnoreCase(request.getName())) {
            variantRepository.findByProductIdAndNameIgnoreCase(productId, request.getName())
                    .ifPresent(v -> {
                        throw BadRequestException.alreadyExits("Variant", v.getId(), request.getName());
                    });
        }
        checkSkuFree(request.getSku(), id);

        variantMapper.updateEntity(variant, request);
        return variantMapper.toResponse(variantRepository.save(variant));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariantResponse> getAll() {
        // an empty list is a valid answer (e.g. after deleting the last variant)
        return variantRepository.findAll().stream().map(variantMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VariantResponse> getPagination(Map<String, String> params) {
        VariantFilter filter = new VariantFilter();
        if (params.containsKey("id")) filter.setId(Long.parseLong(params.get("id")));
        if (params.containsKey("name")) filter.setName(params.get("name"));
        VariantSpec variantSpec = new VariantSpec(filter);
        Pageable pageable = PageUtil.getPageable(params);
        return variantRepository.findAll(variantSpec, pageable).map(variantMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public VariantResponse findById(Long id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Variant", id));
        return variantMapper.toResponse(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariantResponse> findByProductId(Long productId) {
        Product product = productService.findById(productId);
        return variantRepository.findByProductId(product.getId())
                .stream().map(variantMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Variant", id));
        try {
            variantRepository.delete(variant);
            variantRepository.flush(); // runs the SQL now, so a foreign key error is caught here
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(
                    "\"" + variant.getName() + "\" is used by orders or recipes and can't be deleted. "
                            + "Deactivate it instead.");
        }
    }

    @Override
    @Transactional
    public VariantResponse changeStatus(Long id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Variant", id));
        variant.setIsActive(!Boolean.TRUE.equals(variant.getIsActive()));
        return variantMapper.toResponse(variantRepository.save(variant));
    }

    @Override
    @Transactional
    public VariantResponse updatePrice(Long id, BigDecimal price) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Variant", id));
        variant.setPrice(price);
        return variantMapper.toResponse(variantRepository.save(variant));
    }

    // ---------- helpers ----------

    /**
     * Blank -> null (SKU is optional), otherwise trimmed and upper case.
     */
    private String normalizeSku(String sku) {
        return (sku == null || sku.isBlank()) ? null : sku.trim().toUpperCase();
    }

    /**
     * The SKU column is unique across ALL products.
     */
    private void checkSkuFree(String sku, Long currentId) {
        if (sku == null) return;
        Optional<Variant> other = variantRepository.findBySkuIgnoreCase(sku);
        if (other.isPresent() && !other.get().getId().equals(currentId)) {
            throw new BadRequestException(
                    "SKU " + sku + " is already used by \"" + other.get().getName()
                            + "\" (" + other.get().getProduct().getName() + ").");
        }
    }
}