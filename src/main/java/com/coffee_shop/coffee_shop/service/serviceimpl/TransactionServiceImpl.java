package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.PageDTO;
import com.coffee_shop.coffee_shop.dto.response.TransactionResponse;
import com.coffee_shop.coffee_shop.entity.InventoryTransaction;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.TransactionMapper;
import com.coffee_shop.coffee_shop.repository.InventoryTransactionRepository;
import com.coffee_shop.coffee_shop.service.InventoryTransactionService;
import com.coffee_shop.coffee_shop.specification.transaction.TransactionFilter;
import com.coffee_shop.coffee_shop.specification.transaction.TransactionSpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import com.coffee_shop.coffee_shop.util.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements InventoryTransactionService {
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final TransactionMapper transactionMapper;


    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<TransactionResponse> getAll() {
        List<InventoryTransaction> list = inventoryTransactionRepository.findAll();
        if (list.isEmpty()) {
            throw ResourceNotFoundException.notFoundException("Transaction");
        }
        return list.stream().map(transactionMapper::toResponse).toList();
    }

    @Override
    public TransactionResponse getById(Long id) {
        InventoryTransaction transaction = inventoryTransactionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Transaction", id));
        return transactionMapper.toResponse(transaction);
    }

    @Override
    public PageDTO<TransactionResponse> getPagination(Map<String, String> params) {
        TransactionFilter filter = new TransactionFilter();
        if (params.containsKey("id")) {
            filter.setId(Long.valueOf(params.get("id")));
        }
        if (params.containsKey("referenceType")) {
            filter.setReferenceType(params.get("referenceType"));
        }
        if (params.containsKey("referenceId")) {
            filter.setReferenceId(Long.valueOf(params.get("referenceId")));
        }
        if (params.containsKey("startDate")) {
            filter.setStartDate(LocalDateTime.parse(params.get("startDate"), DATE_FORMAT));
        }
        if (params.containsKey("endDate")) {
            filter.setEndDate(LocalDateTime.parse(params.get("endDate"), DATE_FORMAT));
        }

        TransactionSpec spec = new TransactionSpec(filter);
        Pageable pageable = PageUtil.getPageable(params);

        Page<TransactionResponse> page = inventoryTransactionRepository.findAll(spec, pageable)
                .map(transactionMapper::toResponse);
        return new PageDTO<>(page);
    }

    @Override
    public List<TransactionResponse> getByIngredient(Long ingredientId) {
        return inventoryTransactionRepository.findByIngredientId(ingredientId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    public List<TransactionResponse> getByType(TransactionType type) {
        return inventoryTransactionRepository.findByTransactionType(type)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    public List<TransactionResponse> getByDateRange(LocalDateTime start, LocalDateTime end) {
        return inventoryTransactionRepository.findByTransactionDateBetween(start, end)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    public TransactionResponse getSummary() {
        return null;
    }
}
