package com.example.kservertask.order.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateOrderRequest(
        @NotNull
        @Positive
        Long userId,

        @NotNull
        @Positive
        Long menuId,

        @NotBlank
        @Size(max = 100)
        String expectedMenuName,

        @NotNull
        @PositiveOrZero
        Long expectedPrice,

        @NotNull
        @PositiveOrZero
        Long expectedPointVersion
) {
}
