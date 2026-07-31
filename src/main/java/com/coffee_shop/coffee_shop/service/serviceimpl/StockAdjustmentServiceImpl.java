package com.coffee_shop.coffee_shop.service.serviceimpl;

import com.coffee_shop.coffee_shop.dto.response.StockAdjustmentResponse;
import com.coffee_shop.coffee_shop.entity.StockAdjustment;
import com.coffee_shop.coffee_shop.exception.ResourceNotFoundException;
import com.coffee_shop.coffee_shop.mapper.StockAdjustmentMapper;
import com.coffee_shop.coffee_shop.repository.StockAdjustmentRepository;
import com.coffee_shop.coffee_shop.service.StockAdjustmentService;
import com.coffee_shop.coffee_shop.specification.stockAdjustment.StockAdjustmentFilter;
import com.coffee_shop.coffee_shop.specification.stockAdjustment.StockAdjustmentSpec;
import com.coffee_shop.coffee_shop.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StockAdjustmentServiceImpl implements StockAdjustmentService {

    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final StockAdjustmentMapper mapper;


    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<StockAdjustmentResponse> getAdjustmentHistory(Long ingredientId) {
        List<StockAdjustment> history = stockAdjustmentRepository
                .findByIngredientIdOrderByCreatedAtDesc(ingredientId);
        return history.stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<StockAdjustmentResponse> getAll() {
        List<StockAdjustment> all = stockAdjustmentRepository.findAll();
        if (all.isEmpty()) {
            throw ResourceNotFoundException.notFoundException("Stock Adjustment");
        }
        return all.stream().map(mapper::toResponse).toList();
    }

    @Override
    public Page<StockAdjustmentResponse> getAllAdjustments(Map<String, String> params) {
        StockAdjustmentFilter filter = new StockAdjustmentFilter();

        if (params.containsKey("id")) {
            filter.setIngredientId(Long.valueOf(params.get("id")));
        }
        if (params.containsKey("fromDate")) {
            filter.setFromDate(LocalDateTime.parse(params.get("fromDate"), DATE_FORMAT));
        }
        if (params.containsKey("toDate")) {
            filter.setToDate(LocalDateTime.parse(params.get("toDate"), DATE_FORMAT));
        }

        StockAdjustmentSpec spec = new StockAdjustmentSpec(filter);
        Pageable pageable = PageUtil.getPageable(params);

        return stockAdjustmentRepository.findAll(spec, pageable)
                .map(mapper::toResponse);
    }


    @Override
    public List<StockAdjustmentResponse> searchAdjustments(StockAdjustmentFilter filter) {
        Specification<StockAdjustment> spec = new StockAdjustmentSpec(filter);
        List<StockAdjustment> results = stockAdjustmentRepository.findAll(spec);
        return results.stream().map(mapper::toResponse).toList();
    }

    @Override
    public StockAdjustmentResponse getAdjustmentById(Long id) {
        StockAdjustment adjustment = stockAdjustmentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.notFoundException("Stock Adjustment"));
        return mapper.toResponse(adjustment);
    }
}
