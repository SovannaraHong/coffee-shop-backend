package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.AddonCreateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddonResponse;
import com.coffee_shop.coffee_shop.entity.Addon;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.AddonMapper;
import com.coffee_shop.coffee_shop.repository.AddonRepository;
import com.coffee_shop.coffee_shop.service.AddonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AddonServiceImpl implements AddonService {
    private final AddonMapper mapper;
    private final AddonRepository addonRepository;

    @Override
    @Transactional
    public AddonResponse create(AddonCreateRequest request) {
        if (addonRepository.existsByNameIgnoreCase(request.getName())) {
            throw BadRequestException.alreadyExits("Addon", request.getName());
        }
        Addon entity = mapper.toEntity(request);
        return mapper.toAddonResponse(addonRepository.save(entity));
    }

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

    @Override
    @Transactional
    public void delete(Long id) {
        Addon addon = findAddonEntityById(id);
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


    @Override
    @Transactional
    public AddonResponse changeStatus(Long id) {
        Addon addon = findAddonEntityById(id);
        addon.setIsActive(!addon.getIsActive());
        return mapper.toAddonResponse(addonRepository.save(addon));
    }

    private Addon findAddonEntityById(Long id) {
        return addonRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.notFoundException("Addon", id));
    }
}
