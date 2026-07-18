package com.example.kservertask.order.consumer;

import java.time.Instant;
import java.time.LocalDate;

import com.example.kservertask.event.repository.DailyProductSalesRepository;
import com.example.kservertask.event.repository.ProcessedEventRepository;
import com.example.kservertask.order.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final DailyProductSalesRepository salesRepository;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(topics = "order-events", groupId = "popular-menu-group")
    @Transactional
    public void consume(OrderCompletedEvent event) {
        if (processedEventRepository.claimEvent(event.eventId(), Instant.now()) == 0) {
            return;
        }

        LocalDate today = event.orderedAt().toLocalDate();
        salesRepository.upsertSales(event.productId(), today, event.quantity());
    }
}
