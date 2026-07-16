package com.example.kservertask.order.entity;

import com.example.kservertask.base.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(
        name = "coffee_order",
        indexes = @Index(
                name = "idx_coffee_order_popular",
                columnList = "payment_status, paid_at, menu_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoffeeOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(name = "menu_name_snapshot", nullable = false, length = 100)
    private String menuNameSnapshot;

    @Column(name = "payment_amount", nullable = false)
    private long paymentAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus orderStatus;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "paid_at", nullable = false)
    private Instant paidAt;

    public CoffeeOrder(
            Long userId,
            Long menuId,
            String menuNameSnapshot,
            long paymentAmount,
            PaymentStatus paymentStatus,
            OrderStatus orderStatus,
            String idempotencyKey,
            String requestHash,
            Instant paidAt
    ) {
        this.userId = userId;
        this.menuId = menuId;
        this.menuNameSnapshot = menuNameSnapshot;
        this.paymentAmount = paymentAmount;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.paidAt = paidAt;
    }
}
