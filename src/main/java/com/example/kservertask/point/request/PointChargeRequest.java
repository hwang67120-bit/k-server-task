package com.example.kservertask.point.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PointChargeRequest(
        @NotNull
        @Positive
        Long userId,

        @NotBlank
        String phoneNumber,

        @NotBlank
        String paymentId,

        @NotNull
        @Positive
        Long amount
) {
}
