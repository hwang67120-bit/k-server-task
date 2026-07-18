package com.example.kservertask.point.entity;

import com.example.kservertask.base.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point_account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointAccount extends BaseTimeEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private long balance;

    @Version
    @Column(nullable = false)
    private Long version;

    public PointAccount(Long userId, long balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public void addPoint(long amount) {
        this.balance = Math.addExact(this.balance, amount);
    }

    public void deductPoint(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감 포인트는 0보다 커야 합니다.");
        }

        if (balance < amount) {
            throw new IllegalStateException("포인트 잔액이 부족합니다.");
        }

        this.balance -= amount;
    }
}
