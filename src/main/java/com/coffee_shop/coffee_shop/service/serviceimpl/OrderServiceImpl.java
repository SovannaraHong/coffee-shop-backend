package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.request.OrderCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.OrderDetailAddonRequest;
import com.coffee_shop.coffee_shop.dto.request.OrderDetailRequest;
import com.coffee_shop.coffee_shop.dto.response.OrderResponse;
import com.coffee_shop.coffee_shop.entity.*;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.OrderMapper;
import com.coffee_shop.coffee_shop.repository.*;
import com.coffee_shop.coffee_shop.service.OrderService;
import com.coffee_shop.coffee_shop.service.TelegramNotificationService;
import com.coffee_shop.coffee_shop.specification.Order.OrderFilter;
import com.coffee_shop.coffee_shop.specification.Order.OrderSpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import com.coffee_shop.coffee_shop.util.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final VariantRepository variantRepository;
    private final AddonRepository addonRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final OrderMapper orderMapper;
    private final TelegramNotificationService telegramNotificationService;

    private static final DateTimeFormatter ORDER_NUMBER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Customer", request.getCustomerId()));

        Address address = null;
        if (request.getAddressId() != null) {
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> ResourceNotFoundException.notFoundException("Address", request.getAddressId()));
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customer(customer)
                .address(address)
                .status(OrderStatus.PENDING)
                .note(request.getNote())
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .build();

        Set<OrderDetail> orderDetails = buildOrderDetails(order, request.getDetails());
        order.getOrderDetails().addAll(orderDetails);

        BigDecimal totalAmount = calculateTotal(orderDetails);
        order.setTotalAmount(totalAmount);
        order.setFinalAmount(totalAmount.subtract(order.getDiscountAmount()).add(order.getTaxAmount()));

        Order saved = orderRepository.save(order);
        telegramNotificationService.sendMessage(buildOrderTelegramMessage(saved, customer));
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return orderMapper.toResponse(getRequiredOrder(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<OrderResponse> getPagination(Map<String, String> params) {
        OrderFilter filter = new OrderFilter();
        try {
            if (params.containsKey("customerId")) {
                filter.setCustomerId(Long.valueOf(params.get("customerId")));
            }
            if (params.containsKey("status")) {
                filter.setStatus(OrderStatus.valueOf(params.get("status").toUpperCase()));
            }
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid numeric filter value (customerId)");
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status value");
        }

        OrderSpec spec = new OrderSpec(filter);
        Pageable pageable = PageUtil.getPageable(params);

        Page<OrderResponse> page = orderRepository.findAll(spec, pageable).map(orderMapper::toResponse);
        return new PageDTO<>(page);
    }

    @Override
    @Transactional
    public OrderResponse confirm(Long id) {
        Order order = getRequiredOrder(id);
        requireStatus(order, OrderStatus.PENDING, "confirmed");
        order.setStatus(OrderStatus.CONFIRMED);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancel(Long id) {
        Order order = getRequiredOrder(id);
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel an order that is already " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse markPreparing(Long id) {
        Order order = getRequiredOrder(id);
        requireStatus(order, OrderStatus.CONFIRMED, "moved to preparing");
        order.setStatus(OrderStatus.PREPARING);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse markReady(Long id) {
        Order order = getRequiredOrder(id);
        requireStatus(order, OrderStatus.PREPARING, "marked ready");
        order.setStatus(OrderStatus.READY);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse markDelivering(Long id) {
        Order order = getRequiredOrder(id);
        requireStatus(order, OrderStatus.READY, "sent for delivery");
        order.setStatus(OrderStatus.DELIVERING);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse complete(Long id) {
        Order order = getRequiredOrder(id);
        if (order.getStatus() != OrderStatus.READY && order.getStatus() != OrderStatus.DELIVERING) {
            throw new IllegalStateException("Order must be READY or DELIVERING to be completed");
        }

        for (OrderDetail detail : order.getOrderDetails()) {
            deductIngredientsForVariant(detail.getProductVariant(), detail.getQuantity());

            for (OrderDetailAddon addonLine : detail.getOrderDetailAddons()) {
                deductIngredientsForAddon(addonLine.getAddon(), addonLine.getQuantity());
            }
        }

        order.setStatus(OrderStatus.COMPLETED);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    // =========================
    // Private helpers
    // =========================

    private Order getRequiredOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Order", id));
    }

    private void requireStatus(Order order, OrderStatus required, String actionDescription) {
        if (order.getStatus() != required) {
            throw new IllegalStateException(
                    "Order must be " + required + " before it can be " + actionDescription
            );
        }
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(ORDER_NUMBER_DATE_FORMAT);
        String randomPart = String.valueOf((int) (Math.random() * 9000) + 1000);
        return "ORD-" + datePart + "-" + randomPart;
    }

    private Set<OrderDetail> buildOrderDetails(Order order, List<OrderDetailRequest> requests) {
        Set<OrderDetail> details = new HashSet<>();

        for (OrderDetailRequest req : requests) {
            Variant variant = variantRepository.findById(req.getVariantId())
                    .orElseThrow(() -> ResourceNotFoundException.notFoundException("Variant", req.getVariantId()));

            validateVariantIngredients(variant, req.getQuantity());

            BigDecimal unitPrice = variant.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(req.getQuantity()));

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .productVariant(variant)
                    .quantity(req.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            if (req.getAddons() != null && !req.getAddons().isEmpty()) {
                Set<OrderDetailAddon> addons = buildAddons(detail, req.getAddons());
                detail.getOrderDetailAddons().addAll(addons);
            }

            details.add(detail);
        }

        return details;
    }

    private Set<OrderDetailAddon> buildAddons(OrderDetail detail, List<OrderDetailAddonRequest> requests) {
        Set<OrderDetailAddon> addons = new HashSet<>();

        for (OrderDetailAddonRequest req : requests) {
            Addon addon = addonRepository.findById(req.getAddonId())
                    .orElseThrow(() -> ResourceNotFoundException.notFoundException("Addon", req.getAddonId()));

            validateAddonIngredients(addon, req.getQuantity());

            BigDecimal unitPrice = addon.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(req.getQuantity()));

            OrderDetailAddon orderDetailAddon = OrderDetailAddon.builder()
                    .orderDetail(detail)
                    .addon(addon)
                    .quantity(req.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            addons.add(orderDetailAddon);
        }

        return addons;
    }

    private BigDecimal calculateTotal(Set<OrderDetail> details) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetail detail : details) {
            total = total.add(detail.getSubtotal());
            for (OrderDetailAddon addon : detail.getOrderDetailAddons()) {
                total = total.add(addon.getSubtotal());
            }
        }
        return total;
    }

    // ---- Variant (Recipe-based) stock handling ----

    private void validateVariantIngredients(Variant variant, int orderQuantity) {
        Optional<Recipe> recipeOpt = recipeRepository.findByProductVariantId(variant.getId());
        if (recipeOpt.isEmpty()) {
            return; // no recipe -> not ingredient-tracked, always orderable
        }
        for (RecipeIngredient ri : recipeOpt.get().getRecipeIngredients()) {
            checkAvailable(ri.getIngredient(), ri.getQuantityRequired(), orderQuantity);
        }
    }

    private void deductIngredientsForVariant(Variant variant, int orderQuantity) {
        Optional<Recipe> recipeOpt = recipeRepository.findByProductVariantId(variant.getId());
        if (recipeOpt.isEmpty()) {
            return;
        }
        for (RecipeIngredient ri : recipeOpt.get().getRecipeIngredients()) {
            deduct(ri.getIngredient(), ri.getQuantityRequired(), orderQuantity);
        }
    }

    // ---- Addon (AddonIngredient-based) stock handling ----

    private void validateAddonIngredients(Addon addon, int orderQuantity) {
        for (AddonIngredient ai : addon.getAddonIngredients()) {
            checkAvailable(ai.getIngredient(), ai.getQuantityRequired(), orderQuantity);
        }
    }

    private void deductIngredientsForAddon(Addon addon, int orderQuantity) {
        for (AddonIngredient ai : addon.getAddonIngredients()) {
            deduct(ai.getIngredient(), ai.getQuantityRequired(), orderQuantity);
        }
    }

    // ---- Shared ingredient math ----

    private void checkAvailable(Ingredient ingredient, BigDecimal quantityPerUnit, int orderQuantity) {
        BigDecimal totalNeeded = quantityPerUnit.multiply(BigDecimal.valueOf(orderQuantity));
        if (ingredient.getQuantityInStock().compareTo(totalNeeded) < 0) {
            throw new BadRequestException("Insufficient stock for ingredient: " + ingredient.getName());
        }
    }

    private void deduct(Ingredient ingredient, BigDecimal quantityPerUnit, int orderQuantity) {
        BigDecimal totalNeeded = quantityPerUnit.multiply(BigDecimal.valueOf(orderQuantity));
        if (ingredient.getQuantityInStock().compareTo(totalNeeded) < 0) {
            throw new BadRequestException("Insufficient stock for ingredient: " + ingredient.getName());
        }
        ingredient.setQuantityInStock(ingredient.getQuantityInStock().subtract(totalNeeded));
        ingredientRepository.save(ingredient);
    }


    private String buildOrderTelegramMessage(Order saved, Customer customer) {
        StringBuilder sb = new StringBuilder();

        sb.append("🛍️ *New Order Received!*\n");
        sb.append("━━━━━━━━━━━━━━━━\n");
        sb.append("👤 *Customer Info*\n");
        sb.append("🗿 Name: ").append(customer.getFirstName()).append(" ").append(customer.getLastName()).append("\n");
        if (customer.getEmail() != null) {
            sb.append("📧 Email: `").append(customer.getEmail()).append("`\n");
        }
        if (customer.getPhone() != null) {
            sb.append("📲 Phone: `").append(customer.getPhone()).append("`\n");
        }
        sb.append("━━━━━━━━━━━━━━━━\n\n");

        sb.append("🛒 *ORDER ITEMS*\n");
        sb.append("┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄\n");

        for (OrderDetail detail : saved.getOrderDetails()) {
            sb.append("🏷️ ").append(detail.getProductVariant().getProduct().getName())
                    .append(" (").append(detail.getProductVariant().getName()).append(")\n");
            sb.append("💰 Price: `$").append(detail.getUnitPrice()).append("`\n");
            sb.append("🔢 Qty: `").append(detail.getQuantity()).append("`\n");
            sb.append("🧾 Subtotal: `$").append(detail.getSubtotal()).append("`\n");

            if (!detail.getOrderDetailAddons().isEmpty()) {
                sb.append("   ➕ Add-ons:\n");
                for (OrderDetailAddon addon : detail.getOrderDetailAddons()) {
                    sb.append("      • ").append(addon.getAddon().getName())
                            .append(" x").append(addon.getQuantity())
                            .append(" — $").append(addon.getSubtotal()).append("\n");
                }
            }
            sb.append("┄┄┄┄┄┄┄┄┄┄┄┄\n");
        }

        sb.append("\n💵 *Total:* `$").append(saved.getFinalAmount()).append("`\n");
        if (saved.getNote() != null && !saved.getNote().isBlank()) {
            sb.append("📝 Note: ").append(saved.getNote()).append("\n");
        }
        sb.append("🧾 Order #: `").append(saved.getOrderNumber()).append("`\n");
        sb.append("🕐 Order just placed");

        return sb.toString();
    }
}