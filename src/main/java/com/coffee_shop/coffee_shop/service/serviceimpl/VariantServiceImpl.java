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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
    public VariantResponse create(Long productId, VariantRequest variantRequest) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Product", productId));

        Optional<Variant> existing = variantRepository.findByProductIdAndNameIgnoreCase(productId, variantRequest.getName());
        if (existing.isPresent()) {
            throw BadRequestException.alreadyExits("Variant", existing.get().getId(), variantRequest.getName());
        }

        Variant entity = variantMapper.toEntity(variantRequest);
        entity.setProduct(product); // ← the missing link

        return variantMapper.toResponse(variantRepository.save(entity));
    }

    @Override
    @Transactional
    public VariantResponse update(Long id, VariantRequest variantRequest) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Variant", id));

        Long productId = variant.getProduct().getId();

        if (!variant.getName().equalsIgnoreCase(variantRequest.getName())) {
            Optional<Variant> existing = variantRepository
                    .findByProductIdAndNameIgnoreCase(productId, variantRequest.getName());
            if (existing.isPresent()) {
                throw BadRequestException.alreadyExits("Variant", existing.get().getId(), variantRequest.getName());
            }
        }

        variantMapper.updateEntity(variant, variantRequest); // apply new values — this was missing
        return variantMapper.toResponse(variantRepository.save(variant));
    }

    @Override
    public List<VariantResponse> getAll() {
        List<VariantResponse> all = variantRepository.findAll()
                .stream()
                .map(variantMapper::toResponse)
                .toList();
        if (all.isEmpty()) {
            throw ResourceNotFoundException.notFoundException("Variant");
        }
        return all;
    }

    @Override
    public Page<VariantResponse> getPagination(Map<String, String> params) {
        VariantFilter filter = new VariantFilter();
        if (params.containsKey("id")) filter.setId(Long.parseLong(params.get("id")));
        if (params.containsKey("name")) filter.setName(params.get("name"));
        VariantSpec variantSpec = new VariantSpec(filter);
        Pageable pageable = PageUtil.getPageable(params);
        return variantRepository.findAll(variantSpec, pageable).map(variantMapper::toResponse);

    }

    @Override
    public VariantResponse findById(Long id) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Variant", id));
        return variantMapper.toResponse(variant);
    }

    @Override
    public List<VariantResponse> findByProductId(Long productId) {
        Product byId = productService.findById(productId);
        return variantRepository
                .findByProductId(byId.getId()).stream().map(variantMapper::toResponse).toList();
    }

    @Override
    public void delete(Long id) {
        Variant variant = variantRepository
                .findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Variant", id));
        variantRepository.delete(variant);


    }

    @Override
    public VariantResponse changeStatus(Long id) {

        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

        variant.setIsActive(!variant.getIsActive());

        Variant saved = variantRepository.save(variant);

        return variantMapper.toResponse(saved);
    }

    @Override
    public VariantResponse updatePrice(Long id, BigDecimal price) {
        Variant variant = variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

        variant.setPrice(price);
        Variant save = variantRepository.save(variant);

        return variantMapper.toResponse(save);
    }
}
