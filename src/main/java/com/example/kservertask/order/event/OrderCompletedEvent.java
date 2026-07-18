package com.example.kservertask.order.event;

import java.time.LocalDateTime;

public record OrderCompletedEvent(
        String eventId,
        Long productId,
        int quantity,
        LocalDateTime orderedAt
) {
}
