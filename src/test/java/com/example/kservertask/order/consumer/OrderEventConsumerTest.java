package com.example.kservertask.order.consumer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.kservertask.event.repository.DailyProductSalesRepository;
import com.example.kservertask.event.repository.ProcessedEventRepository;
import com.example.kservertask.order.event.OrderCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private DailyProductSalesRepository salesRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    @Test
    void claimsEventAndUpsertsDailySales() {
        OrderCompletedEvent event = new OrderCompletedEvent(
                "event-1",
                10L,
                2,
                LocalDateTime.of(2026, 7, 18, 10, 0)
        );

        when(processedEventRepository.claimEvent(any(), any())).thenReturn(1);

        orderEventConsumer.consume(event);

        verify(salesRepository).upsertSales(10L, LocalDate.of(2026, 7, 18), 2);
    }
}
