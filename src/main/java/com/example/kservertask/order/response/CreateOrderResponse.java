package com.example.kservertask.order.response;

import com.example.kservertask.order.entity.OrderStatus;
import com.example.kservertask.order.entity.PaymentStatus;

public record CreateOrderResponse(
        Long orderId,
        Long menuId,
        String menuName,
        long paymentAmount,
        long remainingPoint,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus,
        long version
) {
}
