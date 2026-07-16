package com.example.kservertask.point.entity;

import com.example.kservertask.base.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_history_id")
    private Long pointHistoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "history_type", nullable = false, length = 20)
    private String historyType;

    @Column(nullable = false)
    private long amount;

    @Column(name = "balance_after", nullable = false)
    private long balanceAfter;

    @Column(name = "version_after", nullable = false)
    private long versionAfter;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    public PointHistory(
            Long userId,
            Long orderId,
            String historyType,
            long amount,
            long balanceAfter,
            long versionAfter,
            String idempotencyKey,
            String requestHash
    ) {
        this.userId = userId;
        this.orderId = orderId;
        this.historyType = historyType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.versionAfter = versionAfter;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
    }
}
