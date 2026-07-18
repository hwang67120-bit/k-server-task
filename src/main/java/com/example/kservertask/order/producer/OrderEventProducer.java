package com.example.kservertask.order.producer;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.kservertask.order.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderEvent(Long productId, int quantity) {
        OrderCompletedEvent event = new OrderCompletedEvent(
                UUID.randomUUID().toString(),
                productId,
                quantity,
                LocalDateTime.now()
        );

        kafkaTemplate.send(TOPIC, String.valueOf(productId), event);
    }
}
