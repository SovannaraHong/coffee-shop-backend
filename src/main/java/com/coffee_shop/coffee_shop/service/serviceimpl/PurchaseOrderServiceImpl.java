package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.PurchaseOrderCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.PurchaseOrderUpdateRequest;
import com.coffee_shop.coffee_shop.dto.request.StockAdjustRequest;
import com.coffee_shop.coffee_shop.dto.response.PurchaseOrderResponse;
import com.coffee_shop.coffee_shop.entity.Ingredient;
import com.coffee_shop.coffee_shop.entity.PurchaseOrder;
import com.coffee_shop.coffee_shop.entity.PurchaseOrderDetail;
import com.coffee_shop.coffee_shop.entity.Supplier;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.PurchaseOrderMapper;
import com.coffee_shop.coffee_shop.repository.IngredientRepository;
import com.coffee_shop.coffee_shop.repository.PurchaseOrderRepository;
import com.coffee_shop.coffee_shop.repository.SupplierRepository;
import com.coffee_shop.coffee_shop.service.IngredientService;
import com.coffee_shop.coffee_shop.service.PurchaseOrderService;
import com.coffee_shop.coffee_shop.specification.purchaseOrder.PurchaseOrderFilter;
import com.coffee_shop.coffee_shop.specification.purchaseOrder.PurchaseOrderSpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import com.coffee_shop.coffee_shop.util.enums.PurchaseOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final IngredientRepository ingredientRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final IngredientService ingredientService;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderCreateRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Supplier", request.getSupplierId()));

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(supplier)
                .orderDate(LocalDateTime.now())
                .status(PurchaseOrderStatus.DRAFT)
                .build();

        List<PurchaseOrderDetail> details = buildDetails(purchaseOrder, request.getDetails());
        purchaseOrder.getPurchaseOrderDetails().addAll(details);
        purchaseOrder.setTotalAmount(calculateTotal(details));

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        return purchaseOrderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse updatePurchaseOrder(Long id, PurchaseOrderUpdateRequest request) {
        PurchaseOrder purchaseOrder = getRequiredPurchaseOrder(id);

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT purchase orders can be edited");
        }

        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() ->
                            ResourceNotFoundException.notFoundException("Supplier", request.getSupplierId()));
            purchaseOrder.setSupplier(supplier);
        }

        if (request.getDetails() != null) {
            // orphanRemoval = true on the entity means clearing + re-adding deletes old rows correctly
            purchaseOrder.getPurchaseOrderDetails().clear();
            List<PurchaseOrderDetail> details = buildDetails(purchaseOrder, request.getDetails());
            purchaseOrder.getPurchaseOrderDetails().addAll(details);
            purchaseOrder.setTotalAmount(calculateTotal(details));
        }

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        return purchaseOrderMapper.toResponse(saved);
    }

    @Override
    public PurchaseOrderResponse getPurchaseOrderById(Long id) {
        return purchaseOrderMapper.toResponse(getRequiredPurchaseOrder(id));
    }

    @Override
    public Page<PurchaseOrderResponse> getPagination(Map<String, String> params) {
        PurchaseOrderFilter filter = new PurchaseOrderFilter();
        if (params.containsKey("id")) {
            filter.setId(Long.valueOf(params.get("id")));
        }
        if (params.containsKey("supplierId")) {
            filter.setSupplierId(Long.valueOf(params.get("supplierId")));
        }
        if (params.containsKey("status")) {
            filter.setStatus(params.get("status"));
        }
        if (params.containsKey("startDate")) {
            filter.setStartDate(LocalDateTime.parse(params.get("startDate"), DATE_FORMAT));
        }
        if (params.containsKey("endDate")) {
            filter.setEndDate(LocalDateTime.parse(params.get("endDate"), DATE_FORMAT));
        }

        PurchaseOrderSpec spec = new PurchaseOrderSpec(filter);
        Pageable pageable = PageUtil.getPageable(params);

        return purchaseOrderRepository.findAll(spec, pageable)
                .map(purchaseOrderMapper::toResponse);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse cancelPurchaseOrder(Long id) {
        PurchaseOrder purchaseOrder = getRequiredPurchaseOrder(id);
        if (purchaseOrder.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new IllegalStateException("Cannot cancel a purchase order that has already been received");
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderResponse orderPurchaseOrder(Long id) {
        PurchaseOrder purchaseOrder = getRequiredPurchaseOrder(id);
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT purchase orders can be submitted");
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.ORDERED);
        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderResponse receivePurchaseOrder(Long id) {
        PurchaseOrder purchaseOrder = getRequiredPurchaseOrder(id);
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.ORDERED) {
            throw new IllegalStateException("Only ORDERED purchase orders can be received");
        }
        // TODO: for each detail line, call ingredientService.increaseStock(...)
        // and create an InventoryTransaction with referenceType = "PURCHASE_ORDER", referenceId = purchaseOrder.getId()

        for (PurchaseOrderDetail detail : purchaseOrder.getPurchaseOrderDetails()) {
            StockAdjustRequest stockRequest = new StockAdjustRequest();
            stockRequest.setQuantity(detail.getQuantity());
            stockRequest.setReferenceType("PURCHASE_ORDER");
            stockRequest.setReferenceId(purchaseOrder.getId());
            stockRequest.setNotes("Received from PO #" + purchaseOrder.getId());

            ingredientService.increaseStock(detail.getIngredient().getId(), stockRequest);
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    private PurchaseOrder getRequiredPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("PurchaseOrder", id));
    }

    private List<PurchaseOrderDetail> buildDetails(
            PurchaseOrder purchaseOrder,
            List<PurchaseOrderCreateRequest.PurchaseOrderDetailRequest> requests
    ) {
        return requests.stream()
                .map(d -> {
                    Ingredient ingredient = ingredientRepository.findById(d.getIngredientId())
                            .orElseThrow(() -> ResourceNotFoundException.notFoundException("Ingredient", d.getIngredientId()));

                    BigDecimal subtotal = d.getQuantity().multiply(d.getUnitCost());

                    return PurchaseOrderDetail.builder()
                            .purchaseOrder(purchaseOrder)
                            .ingredient(ingredient)
                            .quantity(d.getQuantity())
                            .unitCost(d.getUnitCost())
                            .subtotal(subtotal)
                            .build();
                })
                .toList();
    }

    private BigDecimal calculateTotal(List<PurchaseOrderDetail> details) {
        return details.stream()
                .map(PurchaseOrderDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);//find final total
    }
}