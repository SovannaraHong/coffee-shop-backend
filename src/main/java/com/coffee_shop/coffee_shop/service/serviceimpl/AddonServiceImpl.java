package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.AddonCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddonResponse;
import com.coffee_shop.coffee_shop.entity.Addon;
import com.coffee_shop.coffee_shop.entity.Product;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.AddonMapper;
import com.coffee_shop.coffee_shop.repository.AddonRepository;
import com.coffee_shop.coffee_shop.repository.ProductRepository;
import com.coffee_shop.coffee_shop.service.AddonService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AddonServiceImpl implements AddonService {
    private final AddonMapper mapper;
    private final AddonRepository addonRepository;
    // Used ONLY for the built-in findAllById / saveAll. No changes to ProductRepository needed.
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public AddonResponse create(AddonCreateRequest request) {
        if (addonRepository.existsByNameIgnoreCase(request.getName())) {
            throw BadRequestException.alreadyExits("Addon", request.getName());
        }
        Addon entity = mapper.toEntity(request);
        return mapper.toAddonResponse(addonRepository.save(entity));
    }

    // Product responses include the addon name/price/status, so product caches
    // must be cleared whenever an addon changes (otherwise the product list is stale).
    @CacheEvict(value = {"productPagination", "productList"}, allEntries = true)
    @Override
    @Transactional
    public AddonResponse update(Long id, AddonCreateRequest request) {
        Addon addon = findAddonEntityById(id);

        addonRepository.findByNameIgnoreCase(request.getName())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw BadRequestException.alreadyExits("Addon", request.getName());
                });

        mapper.update(addon, request);
        return mapper.toAddonResponse(addonRepository.save(addon));
    }

    @CacheEvict(value = {"productPagination", "productList"}, allEntries = true)
    @Override
    @Transactional
    public void delete(Long id) {
        Addon addon = findAddonEntityById(id);

        // The product/addon join table has an FK to this addon.
        // Return a clear 400 instead of a raw FK error.
        if (addonRepository.isUsedByProducts(id)) {
            throw new BadRequestException(
                    "Add-on '" + addon.getName() + "' is used by one or more products. "
                            + "Remove it from those products first, or set it to inactive instead.");
        }
        addonRepository.delete(addon);
    }

    @Override
    @Transactional(readOnly = true)
    public AddonResponse findById(Long id) {
        return mapper.toAddonResponse(findAddonEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddonResponse> getAll() {
        return addonRepository.findAll().stream()
                .map(mapper::toAddonResponse)
                .toList();
    }

    @CacheEvict(value = {"productPagination", "productList"}, allEntries = true)
    @Override
    @Transactional
    public AddonResponse changeStatus(Long id) {
        Addon addon = findAddonEntityById(id);
        addon.setIsActive(!addon.getIsActive());
        return mapper.toAddonResponse(addonRepository.save(addon));
    }

    // ===================== PRODUCTS OF AN ADDON =====================

    @Override
    @Transactional(readOnly = true)
    public List<Long> getProductIds(Long addonId) {
        findAddonEntityById(addonId);
        return addonRepository.findProductIdsByAddonId(addonId).stream().sorted().toList();
    }

    /**
     * Replace the set of products that offer this addon.
     * Products not in productIds lose the addon; newly selected ones gain it.
     */
    @CacheEvict(value = {"productPagination", "productList"}, allEntries = true)
    @Override
    @Transactional
    public List<Long> assignProducts(Long addonId, Set<Long> productIds) {
        Addon addon = findAddonEntityById(addonId);
        Set<Long> wanted = productIds == null ? Set.of() : productIds;

        List<Product> current = addonRepository.findProductsByAddonId(addonId);
        Set<Long> currentIds = current.stream().map(Product::getId).collect(Collectors.toSet());

        Set<Long> toAddIds = new HashSet<>(wanted);
        toAddIds.removeAll(currentIds);

        // An inactive addon can be removed from products, but not given to new ones.
        if (!toAddIds.isEmpty() && !Boolean.TRUE.equals(addon.getIsActive())) {
            throw new BadRequestException(
                    "Add-on '" + addon.getName() + "' is inactive and cannot be assigned to products");
        }

        List<Product> toAdd = productRepository.findAllById(toAddIds);
        if (toAdd.size() != toAddIds.size()) {
            Set<Long> missing = new HashSet<>(toAddIds);
            toAdd.forEach(p -> missing.remove(p.getId()));
            throw new ResourceNotFoundException("Product(s) not found with id(s): " + missing);
        }

        // Remove from products that are no longer selected
        for (Product p : current) {
            if (!wanted.contains(p.getId()) && p.getAddons() != null) {
                p.getAddons().removeIf(a -> a.getId().equals(addonId));
            }
        }
        // Add to newly selected products
        for (Product p : toAdd) {
            if (p.getAddons() == null) {
                p.setAddons(new HashSet<>());
            }
            p.getAddons().add(addon);
        }

        productRepository.saveAll(current);
        productRepository.saveAll(toAdd);

        return wanted.stream().sorted().toList();
    }

    private Addon findAddonEntityById(Long id) {
        return addonRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.notFoundException("Addon", id));
    }
}