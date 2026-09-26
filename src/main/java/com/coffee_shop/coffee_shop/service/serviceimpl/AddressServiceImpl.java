package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.AddressCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.AddressUpdateRequest;
import com.coffee_shop.coffee_shop.dto.response.AddressResponse;
import com.coffee_shop.coffee_shop.entity.Address;
import com.coffee_shop.coffee_shop.entity.Customer;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.AddressMapper;
import com.coffee_shop.coffee_shop.repository.AddressRepository;
import com.coffee_shop.coffee_shop.repository.CustomerRepository;
import com.coffee_shop.coffee_shop.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final CustomerRepository customerRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public AddressResponse create(AddressCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Customer", request.getCustomerId()));

        Address address = addressMapper.toEntity(request);
        address.setCustomer(customer);
        address.setActive(true);

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            clearExistingDefault(customer.getId());
        }

        Address saved = addressRepository.save(address);
        return addressMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AddressResponse update(Long id, AddressUpdateRequest request) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Address", id));

        addressMapper.updateEntity(request, address);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearExistingDefault(address.getCustomer().getId());
            address.setIsDefault(true);
        }

        Address saved = addressRepository.save(address);
        return addressMapper.toResponse(saved);
    }

    @Override
    public AddressResponse getById(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Address", id));
        return addressMapper.toResponse(address);
    }

    @Override
    public List<AddressResponse> getByCustomerId(Long customerId) {
        return addressRepository.findByCustomerIdAndActiveTrue(customerId)
                .stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Address", id));
        address.setActive(false);
        addressRepository.save(address);
    }

    private void clearExistingDefault(Long customerId) {
        addressRepository.findByCustomerIdAndActiveTrue(customerId).forEach(a -> {
            if (Boolean.TRUE.equals(a.getIsDefault())) {
                a.setIsDefault(false);
                addressRepository.save(a);
            }
        });
    }
}