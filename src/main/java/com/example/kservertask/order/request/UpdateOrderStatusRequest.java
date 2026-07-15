package com.example.kservertask.order.request;

import com.example.kservertask.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateOrderStatusRequest(
        @NotNull
        OrderStatus orderStatus,

        @NotNull
        @PositiveOrZero
        Long expectedVersion
) {
}
