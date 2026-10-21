package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.request.IngredientCreateRequest;
import com.coffee_shop.coffee_shop.dto.request.IngredientUpdateRequest;
import com.coffee_shop.coffee_shop.dto.request.StockAdjustRequest;
import com.coffee_shop.coffee_shop.dto.request.StockSetRequest;
import com.coffee_shop.coffee_shop.dto.response.IngredientResponse;
import com.coffee_shop.coffee_shop.entity.Ingredient;
import com.coffee_shop.coffee_shop.entity.InventoryTransaction;
import com.coffee_shop.coffee_shop.entity.StockAdjustment;
import com.coffee_shop.coffee_shop.exception.BadRequestException;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.IngredientMapping;
import com.coffee_shop.coffee_shop.repository.IngredientRepository;
import com.coffee_shop.coffee_shop.repository.InventoryTransactionRepository;
import com.coffee_shop.coffee_shop.repository.StockAdjustmentRepository;
import com.coffee_shop.coffee_shop.service.IngredientService;
import com.coffee_shop.coffee_shop.specification.ingredient.IngredientFilter;
import com.coffee_shop.coffee_shop.specification.ingredient.IngredientSpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import com.coffee_shop.coffee_shop.util.enums.StockReferenceType;
import com.coffee_shop.coffee_shop.util.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class IngredientServiceImpl implements IngredientService {

    /**
     * Reasons allowed when stock goes UP / DOWN (same rules as the screen)
     */
    private static final Set<StockReferenceType> ADD_REASONS =
            Set.of(StockReferenceType.PURCHASE_ORDER, StockReferenceType.MANUAL);
    private static final Set<StockReferenceType> REMOVE_REASONS =
            Set.of(StockReferenceType.WASTE, StockReferenceType.MANUAL);

    private final IngredientRepository ingredientRepository;
    private final IngredientMapping ingredientMapping;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final StockAdjustmentRepository stockAdjustmentRepository;

    @Transactional
    @Override
    public IngredientResponse createIngredient(IngredientCreateRequest request) {
        Optional<Ingredient> inName = ingredientRepository.findByNameIgnoreCase(request.getName());
        if (inName.isPresent()) {
            throw BadRequestException.alreadyExits("ingredient", inName.get().getId(), request.getName());
        }

        Ingredient entity = ingredientMapping.toEntity(request);
        return ingredientMapping.toResponse(ingredientRepository.save(entity));
    }

    @Transactional
    @Override
    public IngredientResponse updateIngredient(Long id, IngredientUpdateRequest request) {
        Ingredient ingredientById = getIngredientById(id);
        Optional<Ingredient> existsName = ingredientRepository.findByNameIgnoreCase(request.getName());
        if (existsName.isPresent()) {
            if (!ingredientById.getName().equalsIgnoreCase(request.getName())) {
                throw BadRequestException.alreadyExits("Ingredient", existsName.get().getId(), request.getName());
            }
        }

        ingredientMapping.updateEntity(ingredientById, request);

        Ingredient saved = ingredientRepository.save(ingredientById);
        return ingredientMapping.toResponse(saved);
    }

    @Transactional
    @Override
    public void deleteIngredient(Long id) {
        Ingredient ingredientById = getIngredientById(id);
        ingredientRepository.delete(ingredientById);
    }

    @Transactional(readOnly = true)
    @Override
    public Ingredient getIngredientById(Long id) {
        return ingredientRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException
                        .notFoundException("Ingredient", id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository
                .findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(ingredientMapping::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<IngredientResponse> getPagination(Map<String, String> params) {
        IngredientFilter filter = new IngredientFilter();
        if (params.containsKey("id")) filter.setId(Long.parseLong(params.get("id")));
        if (params.containsKey("name")) filter.setName(params.get("name"));
        if (params.containsKey("unit")) filter.setUnit(params.get("unit"));
        IngredientSpec spec = new IngredientSpec(filter);
        Pageable pageable = PageUtil.getPageable(params);
        return ingredientRepository.findAll(spec, pageable).map(ingredientMapping::toResponse);
    }

    // ------------------------------------------------------------------
    // ADD: you know how much came in.
    // Stored in: ingredient.quantity_in_stock + inventory_transaction (IN)
    // ------------------------------------------------------------------
    @Transactional
    @Override
    public IngredientResponse increaseStock(Long id, StockAdjustRequest request) {
        if (!ADD_REASONS.contains(request.getReferenceType())) {
            throw new BadRequestException("Reason " + request.getReferenceType()
                    + " cannot be used when adding stock. Use PURCHASE_ORDER or MANUAL.");
        }

        Ingredient ingredient = getForUpdate(id);
        ingredient.setQuantityInStock(ingredient.getQuantityInStock().add(request.getQuantity()));
        Ingredient saved = ingredientRepository.save(ingredient);

        createInventoryTransaction(
                saved,
                request.getQuantity(),
                TransactionType.IN,
                request.getReferenceType(),
                request.getReferenceId(),
                request.getNotes()
        );
        return ingredientMapping.toResponse(saved);
    }

    // ------------------------------------------------------------------
    // REMOVE: stock left outside of a sale (waste, spoilage...).
    // Stored in: ingredient.quantity_in_stock + inventory_transaction (OUT)
    // ------------------------------------------------------------------
    @Transactional
    @Override
    public IngredientResponse decreaseStock(Long id, StockAdjustRequest request) {
        if (!REMOVE_REASONS.contains(request.getReferenceType())) {
            throw new BadRequestException("Reason " + request.getReferenceType()
                    + " cannot be used when removing stock. Use WASTE or MANUAL.");
        }

        Ingredient ingredient = getForUpdate(id);
        if (ingredient.getQuantityInStock().compareTo(request.getQuantity()) < 0) {
            throw BadRequestException.insufficientStock(
                    ingredient.getName(), ingredient.getQuantityInStock(), request.getQuantity());
        }
        ingredient.setQuantityInStock(ingredient.getQuantityInStock().subtract(request.getQuantity()));
        Ingredient saved = ingredientRepository.save(ingredient);

        createInventoryTransaction(
                saved,
                request.getQuantity(),
                TransactionType.OUT,
                request.getReferenceType(),
                request.getReferenceId(),
                request.getNotes()
        );
        return ingredientMapping.toResponse(saved);
    }

    // ------------------------------------------------------------------
    // SET EXACT: you counted the shelf and the system number is wrong.
    // Stored in: ingredient.quantity_in_stock
    //          + stock_adjustment (old, new, notes)
    //          + inventory_transaction (IN or OUT for the difference, MANUAL)
    // ------------------------------------------------------------------
    @Transactional
    @Override
    public IngredientResponse adjustStock(Long id, StockSetRequest request) {
        Ingredient ingredient = getForUpdate(id);

        BigDecimal oldStock = ingredient.getQuantityInStock();
        BigDecimal newStock = request.getNewQuantity();

        if (oldStock.compareTo(newStock) == 0) {
            return ingredientMapping.toResponse(ingredient); // nothing changed, nothing to record
        }

        ingredient.setQuantityInStock(newStock);
        Ingredient saved = ingredientRepository.save(ingredient);

        // 1) count log
        stockAdjustmentRepository.save(StockAdjustment.builder()
                .ingredient(saved)
                .oldQuantity(oldStock)
                .newQuantity(newStock)
                .notes(request.getNotes())
                .build());

        // 2) history row for the difference
        BigDecimal diff = newStock.subtract(oldStock);
        createInventoryTransaction(
                saved,
                diff.abs(),
                diff.signum() > 0 ? TransactionType.IN : TransactionType.OUT,
                StockReferenceType.MANUAL,
                null,
                "Stock count correction (" + oldStock.stripTrailingZeros().toPlainString()
                        + " -> " + newStock.stripTrailingZeros().toPlainString() + "): "
                        + request.getNotes()
        );

        return ingredientMapping.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<IngredientResponse> checkLowStock() {
        List<Ingredient> lowStockIngredient = ingredientRepository.findLowStockIngredient();
        return lowStockIngredient.stream().map(ingredientMapping::toResponse).toList();
    }

    @Transactional
    @Override
    public IngredientResponse enableIngredient(Long id) {
        Ingredient ingredientById = getIngredientById(id);
        ingredientById.setIsActive(true);

        return ingredientMapping.toResponse(ingredientRepository.save(ingredientById));
    }

    @Transactional
    @Override
    public IngredientResponse disableIngredient(Long id) {
        Ingredient ingredientById = getIngredientById(id);
        ingredientById.setIsActive(false);

        return ingredientMapping.toResponse(ingredientRepository.save(ingredientById));
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /**
     * Loads the ingredient and locks the row until the transaction ends.
     */
    private Ingredient getForUpdate(Long id) {
        return ingredientRepository.findWithLockById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Ingredient", id));
    }

    private void createInventoryTransaction(
            Ingredient ingredient,
            BigDecimal quantity,
            TransactionType type,
            StockReferenceType referenceType,
            Long referenceId,
            String notes
    ) {
        InventoryTransaction transaction = InventoryTransaction.builder()
                .ingredient(ingredient)
                .quantity(quantity)
                .transactionType(type)
                .referenceType(referenceType != null
                        ? referenceType.name()
                        : StockReferenceType.MANUAL.name())
                .referenceId(referenceId)
                .notes(notes)
                .transactionDate(LocalDateTime.now())
                .build();

        inventoryTransactionRepository.save(transaction);
    }
}