package com.example.kservertask.point.response;

import java.time.Instant;

import com.example.kservertask.point.entity.PointHistory;

public record PointHistoryResponse(
        Long pointHistoryId,
        long amount,
        long balanceAfter,
        Instant createdAt
) {

    public static PointHistoryResponse from(PointHistory history) {
        return new PointHistoryResponse(
                history.getPointHistoryId(),
                history.getAmount(),
                history.getBalanceAfter(),
                history.getCreatedAt()
        );
    }
}
