package com.example.kservertask.point.entity;

import com.example.kservertask.base.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "point_charge",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_point_charge_payment_id",
                        columnNames = "payment_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointCharge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "charge_id")
    private Long chargeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "payment_id", nullable = false, length = 100)
    private String paymentId;

    @Column(name = "paid_amount", nullable = false)
    private long paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointChargeStatus status;

    public PointCharge(Long userId, String paymentId, long paidAmount) {
        this.userId = userId;
        this.paymentId = paymentId;
        this.paidAmount = paidAmount;
        this.status = PointChargeStatus.WAITING;
    }

    public void start() {
        this.status = PointChargeStatus.PROCESSING;
    }

    public void complete() {
        this.status = PointChargeStatus.COMPLETED;
    }
}
