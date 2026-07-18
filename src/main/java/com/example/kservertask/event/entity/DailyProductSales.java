package com.example.kservertask.event.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "daily_product_sales",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_product_sales_product_date",
                columnNames = {"product_id", "sales_date"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DailyProductSales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "sales_date", nullable = false)
    private LocalDate salesDate;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "order_count", nullable = false)
    private int orderCount;

    public DailyProductSales(Long productId, LocalDate salesDate, int quantity) {
        this.productId = productId;
        this.salesDate = salesDate;
        this.quantity = quantity;
        this.orderCount = 1;
    }

    public void addSales(int quantity) {
        this.quantity += quantity;
        this.orderCount += 1;
    }
}
