package com.example.kservertask.order.response;

import com.example.kservertask.order.entity.OrderStatus;

public record OrderStatusResponse(
        Long orderId,
        OrderStatus orderStatus,
        long version
) {
}
