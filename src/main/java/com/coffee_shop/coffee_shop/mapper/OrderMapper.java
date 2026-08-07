package com.coffee_shop.coffee_shop.mapper;

import com.coffee_shop.coffee_shop.dto.response.OrderDetailAddonResponse;
import com.coffee_shop.coffee_shop.dto.response.OrderDetailResponse;
import com.coffee_shop.coffee_shop.dto.response.OrderResponse;
import com.coffee_shop.coffee_shop.entity.Customer;
import com.coffee_shop.coffee_shop.entity.Order;
import com.coffee_shop.coffee_shop.entity.OrderDetail;
import com.coffee_shop.coffee_shop.entity.OrderDetailAddon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(target = "customerName", expression = "java(fullName(order.getCustomer()))")

    @Mapping(source = "createdAt", target = "orderDate")
    @Mapping(source = "orderDetails", target = "details")
    OrderResponse toResponse(Order order);

    @Mapping(source = "productVariant.product.name", target = "productName")
    @Mapping(source = "productVariant.id", target = "variantId")
    @Mapping(source = "productVariant.name", target = "variantName")
    @Mapping(source = "orderDetailAddons", target = "addons")
    OrderDetailResponse toDetailResponse(OrderDetail detail);

    @Mapping(source = "addon.id", target = "addonId")
    @Mapping(source = "addon.name", target = "addonName")
    OrderDetailAddonResponse toAddonResponse(OrderDetailAddon addon);

    default String fullName(Customer customer) {
        if (customer == null) return null;
        return customer.getFirstName() + " " + customer.getLastName();
    }
}