package com.example.kservertask.point.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "point_account")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointAccount {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private long balance;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public PointAccount(Long userId, long balance, Instant updatedAt) {
        this.userId = userId;
        this.balance = balance;
        this.updatedAt = updatedAt;
    }
}
