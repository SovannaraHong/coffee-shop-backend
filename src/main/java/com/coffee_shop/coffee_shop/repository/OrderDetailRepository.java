package com.coffee_shop.coffee_shop.repository;

import com.coffee_shop.coffee_shop.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("""
                SELECT COUNT(od) > 0 FROM OrderDetail od
                WHERE od.order.customer.id = :customerId
                AND od.productVariant.product.id = :productId
                AND od.order.status = com.coffee_shop.coffee_shop.util.enums.OrderStatus.COMPLETED
            """)
    boolean existsCompletedPurchase(@Param("customerId") Long customerId, @Param("productId") Long productId);
}