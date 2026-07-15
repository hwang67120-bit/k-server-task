package com.example.kservertask.point.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record AddPointRequest(
        @NotNull
        @Positive
        Long userId,

        @NotNull
        @Positive
        Long amount,

        @NotNull
        @PositiveOrZero
        Long expectedVersion
) {
}
