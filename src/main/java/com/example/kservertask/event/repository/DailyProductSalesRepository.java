package com.example.kservertask.event.repository;

import java.time.LocalDate;

import com.example.kservertask.event.entity.DailyProductSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyProductSalesRepository extends JpaRepository<DailyProductSales, Long> {

    @Modifying
    @Query(value = """
            INSERT INTO daily_product_sales (product_id, sales_date, quantity, order_count)
            VALUES (:productId, :salesDate, :quantity, 1)
            ON DUPLICATE KEY UPDATE
                quantity = quantity + VALUES(quantity),
                order_count = order_count + 1
            """, nativeQuery = true)
    int upsertSales(
            @Param("productId") Long productId,
            @Param("salesDate") LocalDate salesDate,
            @Param("quantity") int quantity
    );
}
