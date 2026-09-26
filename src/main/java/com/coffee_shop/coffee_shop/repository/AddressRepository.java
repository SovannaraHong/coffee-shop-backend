package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerId(Long customerId);

    List<Address> findByCustomerIdAndActiveTrue(Long customerId);
}