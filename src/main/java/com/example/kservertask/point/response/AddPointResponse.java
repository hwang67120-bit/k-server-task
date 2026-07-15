package com.example.kservertask.point.response;

public record AddPointResponse(
        Long chargeId,
        Long userId,
        long chargedAmount,
        long balance,
        long version
) {
}
